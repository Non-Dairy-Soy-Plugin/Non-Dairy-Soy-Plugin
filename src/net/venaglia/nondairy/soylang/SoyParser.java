/*
 * Copyright 2010 - 2012 Ed Venaglia
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package net.venaglia.nondairy.soylang;

import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import net.venaglia.nondairy.soylang.lexer.SoyToken;
import net.venaglia.nondairy.soylang.parser.PsiBuilderTokenSource;
import net.venaglia.nondairy.soylang.parser.SoyStructureParser;
import net.venaglia.nondairy.soylang.parser.TrackedPsiBuilderTokenSource;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

/**
 * User: ed
 * Date: Jul 31, 2010
 * Time: 9:42:17 PM
 *
 * IntelliJ PsiParser implementation for closure templates.
 *
 * This class looks for two system properties that enable enhanced
 * troubleshooting and diagnostics:
 *
 * <code>net.venaglia.nondairy.parser.paranoid = true</code>
 * This will enable the PsiBuilder's debug mode.
 *
 * <code>net.venaglia.nondairy.parser.debugFile=parse-log.html</code>
 * This will record the creation of the FIRST soy psi tree parsed, step by
 * step, to the specified file. This filename should end in ".html".
 */
public class SoyParser implements PsiParser {

    @NonNls
    public static final String PARANOID_PROPERTY = "net.venaglia.nondairy.parser.paranoid";
    @NonNls
    public static final String ENABLE_PARSER_PROPERTY = "net.venaglia.nondairy.parser.enabled";
    @NonNls
    public static final String DEBUG_FILE_PROPERTY = "net.venaglia.nondairy.parser.debugFile";

    private static final boolean PARANOID
            = Boolean.valueOf(System.getProperty(PARANOID_PROPERTY,
                                                 Boolean.toString(false)));
    private static final boolean PARSER_ENABLED
            = Boolean.valueOf(System.getProperty(ENABLE_PARSER_PROPERTY, 
                                                 Boolean.toString(true)));
    private static final AtomicReference<String> LOG_TO_FILE
            = new AtomicReference<String>(System.getProperty(DEBUG_FILE_PROPERTY));

    @NotNull
    public ASTNode parse(IElementType root, PsiBuilder builder) {
        String logToFile = LOG_TO_FILE.getAndSet(null);
        if (PARANOID) {
            builder.setDebugMode(true);
        }
        builder.enforceCommentTokens(TokenSet.create(SoyToken.COMMENT, SoyToken.LINE_COMMENT));
        if (!PARSER_ENABLED) {
            PsiBuilder.Marker file = builder.mark();
            while (!builder.eof()) { // skip everything
                builder.advanceLexer();
            }
            file.done(root);
            return builder.getTreeBuilt();
        }
        if (logToFile != null || PARANOID) {
            TrackedPsiBuilderTokenSource tokenSource = new TrackedPsiBuilderTokenSource(builder);
            PsiBuilder.Marker file = builder.mark();
            while (!tokenSource.eof()) tokenSource.advance();
//            new SoyStructureParser(tokenSource).parse();
            file.done(root);
            try {
                return builder.getTreeBuilt();
            } finally {
                if (logToFile != null) {
                    logTree(tokenSource, logToFile);
                }
            }
        }

        PsiBuilder.Marker file = builder.mark();
        new SoyStructureParser(new PsiBuilderTokenSource(builder)).parse();
        file.done(root);
        return builder.getTreeBuilt();
    }

    @SuppressWarnings("HardCodedStringLiteral")
    public static void logTree(TrackedPsiBuilderTokenSource tokenSource, String fileName) {
        PrintWriter out = null;
        try {
            out = new PrintWriter(new File(fileName));
            if (fileName.toLowerCase().endsWith(".html")) {
                tokenSource.writeHtml(out);
            } else {
                out.println("Plain text report created at " + new Date());
                out.println("** Specify a filename ending in '.html' for HTML output");
                out.println("=====[ Tracing through tree build ]=======================================");
                int lastSeq = tokenSource.getLastSeq();
                out.print(tokenSource.getTree());
                out.printf("  parse completed in %5d operations\n", lastSeq);
                for (int i = 0; i < lastSeq; ++i) {
                    out.printf("-----[ After operation - %5d ]------------------------------------------\n", i);
                    out.print(tokenSource.getTree(i));
                }
                out.println("=====[ Tree build - end of detail ]=========================================");
            }
            out.close();
            out = null;
            System.out.println("Debug output written to file: " + fileName);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
}
