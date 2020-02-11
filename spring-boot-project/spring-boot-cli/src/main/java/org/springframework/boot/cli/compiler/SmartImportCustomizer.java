/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.cli.compiler;

import java.util.LinkedList;
import java.util.List;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.ImportCustomizer;

/**
 * Smart extension of {@link ImportCustomizer} that will only add a specific import if a
 * class with the same name is not already explicitly imported.
 *
 * @author Dave Syer
 */
class SmartImportCustomizer extends ImportCustomizer {

	private SourceUnit source;

	private final List<Import> imports = new LinkedList<>();

	SmartImportCustomizer(SourceUnit source) {
		this.source = source;
	}

	@Override
	public void call(final SourceUnit source, final GeneratorContext context, final ClassNode classNode) {
		ModuleNode ast = source.getAST();

		for (Import anImport : this.imports) {
			switch (anImport.type) {
			case regular:
				ast.addImport(anImport.alias, anImport.classNode);
				break;
			case staticImport:
				ast.addStaticImport(anImport.classNode, anImport.field, anImport.alias);
				break;
			case staticStar:
				ast.addStaticStarImport(anImport.alias, anImport.classNode);
				break;
			case star:
				ast.addStarImport(anImport.star);
				break;
			}
		}
	}

	@Override
	public ImportCustomizer addStaticImport(final String className, final String fieldName) {
		this.imports.add(new Import(ImportType.staticImport, fieldName, ClassHelper.make(className), fieldName));
		return this;
	}

	@Override
	public ImportCustomizer addStaticImport(final String alias, final String className, final String fieldName) {
		this.imports.add(new Import(ImportType.staticImport, alias, ClassHelper.make(className), fieldName));
		return this;
	}

	@Override
	public ImportCustomizer addStarImports(final String... packageNames) {
		for (String packageName : packageNames) {
			addStarImport(packageName);
		}
		return this;
	}

	@Override
	public ImportCustomizer addStaticStars(final String... classNames) {
		for (String className : classNames) {
			addStaticStar(className);
		}
		return this;
	}

	@Override
	public ImportCustomizer addImport(String alias, String className) {
		if (this.source.getAST().getImport(ClassHelper.make(className).getNameWithoutPackage()) == null) {
			this.imports.add(new Import(ImportType.regular, alias, ClassHelper.make(className)));
		}
		return this;
	}

	@Override
	public ImportCustomizer addImports(String... imports) {
		for (String alias : imports) {
			if (this.source.getAST().getImport(ClassHelper.make(alias).getNameWithoutPackage()) == null) {
				addImport(alias);
			}
		}
		return this;
	}

	private void addImport(final String className) {
		ClassNode node = ClassHelper.make(className);
		this.imports.add(new Import(ImportType.regular, node.getNameWithoutPackage(), node));
	}

	private void addStarImport(final String packageName) {
		this.imports.add(new Import(ImportType.star, packageName.endsWith(".") ? packageName : packageName + "."));
	}

	private void addStaticStar(final String className) {
		this.imports.add(new Import(ImportType.staticStar, className, ClassHelper.make(className)));
	}

	/**
	 * Represents imports which are possibly aliased.
	 */
	private static final class Import {

		final ImportType type;

		final ClassNode classNode;

		final String alias;

		final String field;

		final String star; // only used for star imports

		private Import(final ImportType type, final String alias, final ClassNode classNode, final String field) {
			this.alias = alias;
			this.classNode = classNode;
			this.field = field;
			this.type = type;
			this.star = null;
		}

		private Import(final ImportType type, final String alias, final ClassNode classNode) {
			this.alias = alias;
			this.classNode = classNode;
			this.type = type;
			this.field = null;
			this.star = null;
		}

		private Import(final ImportType type, final String star) {
			this.type = type;
			this.star = star;
			this.alias = null;
			this.classNode = null;
			this.field = null;
		}

	}

	private enum ImportType {

		regular, staticImport, staticStar, star

	}

}
