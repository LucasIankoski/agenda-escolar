package com.escolar.agenda.service;

import com.escolar.agenda.config.GalleryStorageProperties;
import com.escolar.agenda.entity.Student;
import com.escolar.agenda.entity.StudentGalleryPhoto;
import com.escolar.agenda.entity.UserApp;
import com.escolar.agenda.enums.UserType;
import com.escolar.agenda.repository.StudentGalleryPhotoRepository;
import com.escolar.agenda.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentGalleryService {

	private static final int MAX_FILES_PER_REQUEST = 10;
	private static final long MAX_FILE_SIZE_BYTES = 10L * 1024L * 1024L;
	private static final int THUMBNAIL_MAX_DIMENSION = 640;

	private final StudentGalleryPhotoRepository studentGalleryPhotoRepository;
	private final StudentRepository studentRepository;
	private final GalleryStorageProperties galleryStorageProperties;

	@Transactional(readOnly = true)
	public List<StudentGalleryPhoto> listByStudent(UUID studentId, UserApp loggedUser) {
		Student student = getStudentOrThrow(studentId);
		validateStudentAccess(student, loggedUser);
		return studentGalleryPhotoRepository.findAllByStudentIdOrderByCreatedAtDesc(studentId);
	}

	@Transactional
	public List<StudentGalleryPhoto> create(UUID studentId, String caption, List<MultipartFile> files, UserApp loggedUser) {
		validateTeacherOrAdmin(loggedUser);

		Student student = getStudentOrThrow(studentId);
		validateStudentAccess(student, loggedUser);

		if (files == null || files.isEmpty()) {
			throw new IllegalArgumentException("Selecione ao menos uma imagem.");
		}
		if (files.size() > MAX_FILES_PER_REQUEST) {
			throw new IllegalArgumentException("Envie no maximo 10 imagens por vez.");
		}

		String normalizedCaption = normalizeCaption(caption);
		List<StudentGalleryPhoto> savedPhotos = new ArrayList<>();
		List<Path> storedPaths = new ArrayList<>();

		try {
			for (MultipartFile file : files) {
				StoredPhoto storedPhoto = storePhoto(student.getId(), file);
				storedPaths.add(storedPhoto.originalPath());
				if (!storedPhoto.thumbnailPath().equals(storedPhoto.originalPath())) {
					storedPaths.add(storedPhoto.thumbnailPath());
				}

				StudentGalleryPhoto photo = new StudentGalleryPhoto();
				photo.setStudent(student);
				photo.setCreatedBy(loggedUser);
				photo.setCaption(normalizedCaption);
				photo.setOriginalFileName(storedPhoto.originalFileName());
				photo.setStoragePath(storedPhoto.storagePath());
				photo.setThumbnailPath(storedPhoto.thumbnailStoragePath());
				photo.setContentType(storedPhoto.contentType());
				photo.setSizeInBytes(storedPhoto.sizeInBytes());
				photo.setWidth(storedPhoto.width());
				photo.setHeight(storedPhoto.height());
				savedPhotos.add(studentGalleryPhotoRepository.save(photo));
			}

			return savedPhotos;
		} catch (IOException e) {
			cleanupStoredFiles(storedPaths);
			throw new IllegalStateException("Falha ao armazenar as fotos da galeria.", e);
		} catch (RuntimeException e) {
			cleanupStoredFiles(storedPaths);
			throw e;
		}
	}

	@Transactional(readOnly = true)
	public GalleryBinary loadOriginal(UUID studentId, UUID photoId, UserApp loggedUser) {
		StudentGalleryPhoto photo = getPhotoOrThrow(studentId, photoId);
		validateStudentAccess(photo.getStudent(), loggedUser);
		return loadBinary(photo.getStoragePath(), photo.getContentType(), photo.getOriginalFileName());
	}

	@Transactional(readOnly = true)
	public GalleryBinary loadThumbnail(UUID studentId, UUID photoId, UserApp loggedUser) {
		StudentGalleryPhoto photo = getPhotoOrThrow(studentId, photoId);
		validateStudentAccess(photo.getStudent(), loggedUser);
		return loadBinary(photo.getThumbnailPath(), photo.getContentType(), photo.getOriginalFileName());
	}

	private GalleryBinary loadBinary(String relativePath, String contentType, String fileName) {
		try {
			Path filePath = resolveStoragePath(relativePath);
			if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
				throw new NoSuchElementException("Arquivo da galeria nao encontrado.");
			}

			Resource resource = new UrlResource(filePath.toUri());
			return new GalleryBinary(resource, contentType, Files.size(filePath), fileName);
		} catch (IOException e) {
			throw new UncheckedIOException("Falha ao carregar arquivo da galeria.", e);
		}
	}

	private StoredPhoto storePhoto(UUID studentId, MultipartFile file) throws IOException {
		validateFile(file);

		ImageFormat imageFormat = resolveImageFormat(file);
		byte[] bytes = file.getBytes();
		BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(bytes));
		if (originalImage == null) {
			throw new IllegalArgumentException("Formato de imagem nao suportado. Use JPG ou PNG.");
		}

		Path rootPath = ensureStorageRoot();
		Path directory = rootPath.resolve(studentId.toString()).resolve(YearMonth.now().toString()).normalize();
		ensurePathInsideRoot(rootPath, directory);
		Files.createDirectories(directory);

		String token = UUID.randomUUID().toString();
		Path originalPath = directory.resolve(token + "_orig." + imageFormat.extension()).normalize();
		ensurePathInsideRoot(rootPath, originalPath);
		Files.write(originalPath, bytes, StandardOpenOption.CREATE_NEW);

		BufferedImage thumbnailImage = resizeImage(originalImage, THUMBNAIL_MAX_DIMENSION, imageFormat.supportsAlpha());
		Path thumbnailPath = originalPath;
		if (thumbnailImage.getWidth() < originalImage.getWidth() || thumbnailImage.getHeight() < originalImage.getHeight()) {
			thumbnailPath = directory.resolve(token + "_thumb." + imageFormat.extension()).normalize();
			ensurePathInsideRoot(rootPath, thumbnailPath);
			ImageIO.write(thumbnailImage, imageFormat.writerFormat(), thumbnailPath.toFile());
		}

		return new StoredPhoto(
				originalPath,
				thumbnailPath,
				toRelativeStoragePath(rootPath, originalPath),
				toRelativeStoragePath(rootPath, thumbnailPath),
				sanitizeOriginalFileName(file.getOriginalFilename(), imageFormat.extension()),
				imageFormat.contentType(),
				file.getSize(),
				originalImage.getWidth(),
				originalImage.getHeight()
		);
	}

	private void validateFile(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("Uma das imagens enviadas esta vazia.");
		}
		if (file.getSize() > MAX_FILE_SIZE_BYTES) {
			throw new IllegalArgumentException("Cada imagem deve ter no maximo 10 MB.");
		}
	}

	private ImageFormat resolveImageFormat(MultipartFile file) {
		String contentType = Optional.ofNullable(file.getContentType()).orElse("").toLowerCase(Locale.ROOT);
		String originalName = Optional.ofNullable(file.getOriginalFilename()).orElse("").toLowerCase(Locale.ROOT);

		if ("image/png".equals(contentType) || originalName.endsWith(".png")) {
			return ImageFormat.PNG;
		}

		if ("image/jpeg".equals(contentType)
				|| "image/jpg".equals(contentType)
				|| originalName.endsWith(".jpg")
				|| originalName.endsWith(".jpeg")) {
			return ImageFormat.JPEG;
		}

		throw new IllegalArgumentException("Formato de imagem nao suportado. Use JPG ou PNG.");
	}

	private BufferedImage resizeImage(BufferedImage source, int maxDimension, boolean preserveAlpha) {
		int sourceWidth = source.getWidth();
		int sourceHeight = source.getHeight();

		if (sourceWidth <= maxDimension && sourceHeight <= maxDimension) {
			return source;
		}

		double scale = Math.min((double) maxDimension / sourceWidth, (double) maxDimension / sourceHeight);
		int targetWidth = Math.max(1, (int) Math.round(sourceWidth * scale));
		int targetHeight = Math.max(1, (int) Math.round(sourceHeight * scale));
		int imageType = preserveAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;

		BufferedImage resized = new BufferedImage(targetWidth, targetHeight, imageType);
		Graphics2D graphics = resized.createGraphics();
		try {
			if (!preserveAlpha) {
				graphics.setColor(Color.WHITE);
				graphics.fillRect(0, 0, targetWidth, targetHeight);
			}
			graphics.setComposite(AlphaComposite.SrcOver);
			graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			graphics.drawImage(source, 0, 0, targetWidth, targetHeight, null);
		} finally {
			graphics.dispose();
		}

		return resized;
	}

	private String normalizeCaption(String caption) {
		if (caption == null) {
			return null;
		}

		String trimmed = caption.trim();
		if (trimmed.isEmpty()) {
			return null;
		}
		if (trimmed.length() > 240) {
			throw new IllegalArgumentException("Legenda deve ter no maximo 240 caracteres.");
		}
		return trimmed;
	}

	private Path ensureStorageRoot() throws IOException {
		Path rootPath = galleryStorageProperties.resolveRootPath();
		Files.createDirectories(rootPath);
		return rootPath;
	}

	private Path resolveStoragePath(String relativePath) {
		Path rootPath = galleryStorageProperties.resolveRootPath();
		Path resolvedPath = rootPath.resolve(relativePath).normalize();
		ensurePathInsideRoot(rootPath, resolvedPath);
		return resolvedPath;
	}

	private void ensurePathInsideRoot(Path rootPath, Path candidatePath) {
		if (!candidatePath.startsWith(rootPath)) {
			throw new IllegalStateException("Caminho de armazenamento invalido.");
		}
	}

	private String toRelativeStoragePath(Path rootPath, Path filePath) {
		return rootPath.relativize(filePath).toString().replace('\\', '/');
	}

	private String sanitizeOriginalFileName(String originalFileName, String extension) {
		if (originalFileName == null || originalFileName.isBlank()) {
			return "foto." + extension;
		}

		String sanitized = originalFileName.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
		if (sanitized.isEmpty()) {
			return "foto." + extension;
		}
		return sanitized;
	}

	private void cleanupStoredFiles(List<Path> paths) {
		for (Path path : paths) {
			try {
				Files.deleteIfExists(path);
			} catch (IOException ignored) {
				// cleanup attempt only
			}
		}
	}

	private StudentGalleryPhoto getPhotoOrThrow(UUID studentId, UUID photoId) {
		return studentGalleryPhotoRepository.findByIdAndStudentId(photoId, studentId)
				.orElseThrow(() -> new NoSuchElementException("Foto da galeria nao encontrada."));
	}

	private Student getStudentOrThrow(UUID studentId) {
		return studentRepository.findById(studentId)
				.orElseThrow(() -> new NoSuchElementException("Aluno nao encontrado."));
	}

	private void validateStudentAccess(Student student, UserApp loggedUser) {
		if (isTeacherOrAdmin(loggedUser)) {
			return;
		}

		if (isParent(loggedUser)
				&& student.getParentUser() != null
				&& student.getParentUser().getId().equals(loggedUser.getId())) {
			return;
		}

		throw new AccessDeniedException("Voce nao tem acesso a este aluno.");
	}

	private void validateTeacherOrAdmin(UserApp loggedUser) {
		if (!isTeacherOrAdmin(loggedUser)) {
			throw new AccessDeniedException("Apenas professor ou admin pode publicar fotos.");
		}
	}

	private boolean isParent(UserApp user) {
		return user.getType() == UserType.PAI;
	}

	private boolean isTeacherOrAdmin(UserApp user) {
		return user.getType() == UserType.PROFESSOR || user.getType() == UserType.ADMIN;
	}

	public record GalleryBinary(Resource resource, String contentType, long contentLength, String fileName) {
	}

	private record StoredPhoto(
			Path originalPath,
			Path thumbnailPath,
			String storagePath,
			String thumbnailStoragePath,
			String originalFileName,
			String contentType,
			long sizeInBytes,
			int width,
			int height
	) {
	}

	private enum ImageFormat {
		JPEG("jpg", "jpg", "image/jpeg", false),
		PNG("png", "png", "image/png", true);

		private final String extension;
		private final String writerFormat;
		private final String contentType;
		private final boolean supportsAlpha;

		ImageFormat(String extension, String writerFormat, String contentType, boolean supportsAlpha) {
			this.extension = extension;
			this.writerFormat = writerFormat;
			this.contentType = contentType;
			this.supportsAlpha = supportsAlpha;
		}

		public String extension() {
			return extension;
		}

		public String writerFormat() {
			return writerFormat;
		}

		public String contentType() {
			return contentType;
		}

		public boolean supportsAlpha() {
			return supportsAlpha;
		}
	}
}
