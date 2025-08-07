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
import com.dsl.classgen.io.SupportProvider;
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

public final class SyncSource extends SupportProvider implements SyncOperations {

	private CompilationUnit cUnit = getNewCompilationUnit(pathsCtx.getExistingPJavaGeneratedSourcePath());
	private Consumer<Void> consumeWriter = _ -> Writer.write(pathsCtx.getExistingPJavaGeneratedSourcePath(), cUnit.toString());
	
	@Override
	public void insertClassSection(List<Path> pathList) {
		LOGGER.log(LogLevels.NOTICE.getLevel(), "Generating new data entries...");
		
		/*
		 * BUG: ao inserir uma nova classe, o cache nao e atualizado
		 */
		pathList.forEach(path -> {
			InnerStaticClassModel model = InnerStaticClassModel.initInstance(path);
			OutterClassModel.computeClassModelToMap(model);
			CacheManager.computeCacheModelToMap(path, new CacheModel(model));
			
			ClassOrInterfaceDeclaration classDecl = innerClassGen.generateData(model);
			cUnit.getClassByName(pathsCtx.getOutterClassName()).ifPresent(c -> c.addMember(classDecl));
		});
		
		consumeWriter.accept(null);
	}
	
	@Override
	public void eraseClassSection(List<CacheModel> currentCacheModelList) {
		LOGGER.log(LogLevels.NOTICE.getLevel(), "Erasing class section...");
		List<Class<?>> filteredClassList = AnnotationProcessor.processClassAnnotations(currentCacheModelList);

		cUnit.findAll(ClassOrInterfaceDeclaration.class)
				.stream()
				.filter(classDecl -> filteredClassList.stream()
						.anyMatch(clazz -> clazz.getSimpleName().equals(classDecl.getNameAsString())))
				.forEach(Node::remove);
		consumeWriter.accept(null);
	}
	
	@Override
	public void modifySection(Map<SyncOptions, Map<Integer, CachePropertiesData>> mappedChanges, CacheModel currentCacheModel) {
		LOGGER.log(LogLevels.NOTICE.getLevel(), "Modifying source entries...");
		
		mappedChanges.entrySet().forEach(entry -> {
			Supplier<Stream<Map.Entry<Integer,CachePropertiesData>>> streamEntry = () -> entry.getValue().entrySet().stream();
			
			// TODO: implementar a lógica de modificação de campos com o enum 'MODIFY'
			switch(entry.getKey()) {
				case INSERT: 
					List<FieldDeclaration> fieldDeclList = innerFieldGen.generateData(streamEntry.get()
							   			.map(entries -> OutterClassModel.getModel(currentCacheModel.filePath)
									   						.insertNewModel(entries.getValue().propKey(), 
									   										entries.getValue().rawPropValue(), 
											   								currentCacheModel.parseJavaType()))
							   			.toList());
					cUnit.findAll(ClassOrInterfaceDeclaration.class)
							.stream()
							.filter(classDecl -> classDecl.getNameAsString().equals(OutterClassModel.getModel(currentCacheModel.filePath).className()))
							.forEach(cls -> fieldDeclList.forEach(cls::addMember));
					break;
					
				case DELETE:
					/*
					 *  BUG: regeneracao precoce de todo o código fonte da classe ao invés de apenas os campos.
					 *  Isso pode levar a duplicacoes de classes no código fonte.
					 */
					List<Field> fieldList = streamEntry.get()
							   .map(entries -> AnnotationProcessor.processFieldAnnotations(currentCacheModel.fileHash, entries.getKey()))
							   .toList();
					
					cUnit.findAll(ClassOrInterfaceDeclaration.class)
							.stream()
							.filter(classDecl -> classDecl.getNameAsString().equals(OutterClassModel.getModel(currentCacheModel.filePath).className()))
							.flatMap(classDecl -> classDecl.findAll(FieldDeclaration.class).stream())
							.filter(fieldDecl -> fieldDecl.getVariables().stream()
								.anyMatch(f -> fieldList.stream()
										.anyMatch(field -> field.getName().equals(f.getNameAsString()))))
							.forEach(Node::remove);
					break;
			}
		});
		consumeWriter.accept(null);
		CacheManager.processCache();
	}

	public <T> void insertClassSection(T path) {
		insertClassSection(List.of(Path.of(path.toString())));
	}
	
	public void eraseClassSection(CacheModel currentCacheModel) {
		eraseClassSection(List.of(currentCacheModel));
	}
	
}
