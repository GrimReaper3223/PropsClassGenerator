package com.dsl.classgen.io.synchronizer;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.dsl.classgen.annotation.processors.AnnotationProcessor;
import com.dsl.classgen.io.CacheManager;
import com.dsl.classgen.io.file_manager.Writer;
import com.dsl.classgen.models.CacheModel;
import com.dsl.classgen.models.CachePropertiesData;
import com.dsl.classgen.models.model_mapper.InnerStaticClassModel;
import com.dsl.classgen.models.model_mapper.OutterClassModel;
import com.dsl.classgen.utils.LogLevels;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;

/**
 * The Class SyncSource.
 */
public final class SyncSource implements SyncOperations {

	private CompilationUnit cUnit = getNewCompilationUnit(pathsCtx.getExistingPJavaGeneratedSourcePath());

	/**
	 * Writes data to a contextualized file, using the existing cUnit as content.
	 */
	private Consumer<Void> consumeWriter = _ -> Writer.write(pathsCtx.getExistingPJavaGeneratedSourcePath(),
			cUnit.toString());

	/**
	 * Insert class section to the source file. Perform operation in a batch manner,
	 * processing a list of paths.
	 *
	 * @param pathList the path list to process
	 */
	@Override
	public void insertClassSection(List<Path> pathList) {
		LOGGER.log(LogLevels.NOTICE.getLevel(), "Generating new class entries...");

		pathList.forEach(path -> {
			InnerStaticClassModel model = InnerStaticClassModel.initInstance(path);
			new CacheModel(model);

			ClassOrInterfaceDeclaration classDecl = innerClassGen.generateData(model);
			cUnit.getClassByName(pathsCtx.getOutterClassName()).ifPresent(c -> c.addMember(classDecl));
		});
		consumeWriter.accept(null);
		CacheManager.processCache();
	}

	/**
	 * Erase class section to the source file. Perform operation in a batch manner,
	 * processing a list of paths.
	 *
	 * @param currentCacheModelList the current cache model list to process
	 */
	@Override
	public void eraseClassSection(List<CacheModel> currentCacheModelList) {
		LOGGER.log(LogLevels.NOTICE.getLevel(), "Erasing class entries...");
		List<Class<?>> filteredClassList = AnnotationProcessor.processClassAnnotations(currentCacheModelList);

		cUnit.findAll(ClassOrInterfaceDeclaration.class).stream()
				.filter(classDecl -> filteredClassList.stream()
						.anyMatch(clazz -> clazz.getSimpleName().equals(classDecl.getNameAsString())))
				.forEach(Node::remove);
		consumeWriter.accept(null);
	}

	/**
	 * Modify section of the source file based on mapped changes.
	 *
	 * @param mappedChanges     the mapped changes
	 * @param currentCacheModel the current cache model representing the current
	 *                          state
	 */
	@Override
	public void modifySection(Map<SyncOptions, Map<Integer, CachePropertiesData>> mappedChanges,
			CacheModel currentCacheModel) {
		LOGGER.log(LogLevels.NOTICE.getLevel(), "Modifying source entries...");

		mappedChanges.entrySet().forEach(entry -> {
			Supplier<Stream<Map.Entry<Integer, CachePropertiesData>>> streamEntry = () -> entry.getValue().entrySet()
					.stream();

			// TODO: implementar a lógica de modificação de campos com o enum 'MODIFY'
			switch (entry.getKey()) {
				case INSERT -> cUnit.findAll(ClassOrInterfaceDeclaration.class).stream()
						.filter(classDecl -> classDecl.getNameAsString()
								.equals(OutterClassModel.getModel(currentCacheModel.filePath).className()))
						.forEach(cls -> generateFieldsDeclarations(streamEntry.get(), currentCacheModel)
								.forEach(cls::addMember));

				case DELETE -> cUnit.findAll(ClassOrInterfaceDeclaration.class).stream()
						.filter(classDecl -> classDecl.getNameAsString()
								.equals(OutterClassModel.getModel(currentCacheModel.filePath).className()))
						.flatMap(classDecl -> classDecl.findAll(FieldDeclaration.class).stream())
						.filter(fieldDecl -> fieldDecl.getVariables().stream()
								.anyMatch(f -> getGeneratedFields(streamEntry.get(), currentCacheModel).stream()
										.anyMatch(field -> field.getName().equals(f.getNameAsString()))))
						.forEach(Node::remove);
			}
		});
		consumeWriter.accept(null);
		CacheManager.processCache();
	}

	/**
	 * Insert class section to the source file. Performs operation on a single path.
	 *
	 * @param <T>  the generic type to be associated with the argument (String or
	 *             Path)
	 * @param path the properties file path
	 */
	public <T> void insertClassSection(T path) {
		insertClassSection(List.of(Path.of(path.toString())));
	}

	/**
	 * Erase class section to the compiled class file. Performs operation on a
	 * single path.
	 *
	 * @param currentCacheModel the current cache model to process
	 */
	public void eraseClassSection(CacheModel currentCacheModel) {
		eraseClassSection(List.of(currentCacheModel));
	}

	/**
	 * Generate fields declarations.
	 *
	 * @param entries           the entries containing property data to generate
	 *                          fields
	 * @param currentCacheModel the current cache model representing the current
	 *                          state
	 * @return the list of field declarations
	 */
	private List<FieldDeclaration> generateFieldsDeclarations(Stream<Map.Entry<Integer, CachePropertiesData>> entries,
			CacheModel currentCacheModel) {
		return innerFieldGen.generateData(entries
				.map(entry -> OutterClassModel.getModel(currentCacheModel.filePath).insertNewModel(
						entry.getValue().propKey(), entry.getValue().rawPropValue(), currentCacheModel.parseJavaType()))
				.toList());
	}

	/**
	 * Gets the generated fields.
	 *
	 * @param entries           the entries containing property data to find fields
	 *                          for generation
	 * @param currentCacheModel the current cache model representing the current
	 *                          state
	 * @return the generated fields
	 */
	private List<Field> getGeneratedFields(Stream<Map.Entry<Integer, CachePropertiesData>> entries,
			CacheModel currentCacheModel) {
		return entries
				.map(entry -> AnnotationProcessor.processFieldAnnotations(currentCacheModel.fileHash, entry.getKey()))
				.toList();
	}
}
