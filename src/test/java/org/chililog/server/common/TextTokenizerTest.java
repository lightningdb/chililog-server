//
// Copyright 2010 Cinch Logic Pty Ltd.
//
// http://www.chililog.com
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package org.chililog.server.common;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.Version;
import org.chililog.server.common.Log4JLogger;
import org.chililog.server.common.TextTokenizer;
import org.junit.Test;

public class TextTokenizerTest {
    private static Log4JLogger _logger = Log4JLogger.getLogger(TextTokenizerTest.class);

    @Test
    public void testBasic() throws IOException {
        List<String> text = TextTokenizer.getInstance().tokenize("Hello, I am Jim.", -1);
        _logger.info(text.toString());
        assertEquals(4, text.size());
        assertEquals("[hello, i, am, jim]", text.toString());

        // Apache logs http://httpd.apache.org/docs/1.3/logs.html
        List<String> apacheError = TextTokenizer
                .getInstance()
                .tokenize(
                        "[Wed Oct 11 14:32:52 2000] [error] [client 127.0.0.1] client denied by server configuration: /export/home/live/ap/htdocs/test",
                        -1);
        _logger.info(apacheError.toString());

        List<String> commonLogFormat = TextTokenizer.getInstance().tokenize(
                "127.0.0.1 - frank [10/Oct/2000:13:55:36 -0700] \"GET /apache_pb.gif HTTP/1.0\" 200 2326", -1);
        _logger.info(commonLogFormat.toString());

        List<String> combinedLogFormat = TextTokenizer
                .getInstance()
                .tokenize(
                        "127.0.0.1 - frank [10/Oct/2000:13:55:36 -0700] \"GET /apache_pb.gif HTTP/1.0\" 200 2326 \"http://www.example.com/start.html\" \"Mozilla/4.08 [en] (Win98; I ;Nav)\"",
                        -1);
        _logger.info(combinedLogFormat.toString());

        // Email and file path
        List<String> emails = TextTokenizer
                .getInstance()
                .tokenize(
                        "vibul@testing.com.au is the email address to parse. C:\\folder1\\folder2\\vvv.java. /tmp/test/vvv.java",
                        -1);
        _logger.info(emails.toString());
        assertEquals("[vibul, testing, com, au, email, address, parse, c, folder1, folder2, vvv, java, tmp, test]",
                emails.toString());

        // xml
        List<String> xml = TextTokenizer.getInstance().tokenize("<hello><afield>b</afield></hello>", -1);
        _logger.info(xml.toString());

        // json
        List<String> json = TextTokenizer.getInstance().tokenize(
                "{ name: \"chililog\", display_name: \"ChiliLog Log\", "
                        + "description: \"Log repository for ChiliLog events\", startup_status: 'ONLINE'}", -1);
        _logger.info(json.toString());

        // stack trace
        List<String> stackTrace = TextTokenizer
                .getInstance()
                .tokenize(
                        "2011-03-26 15:32:22,376 [main] ERROR com.chililog.server.common.ChiliLogExceptionTest - "
                                + "com.chililog.server.common.ChiliLogException: Test12\n"
                                + "at com.chililog.server.common.ChiliLogExceptionTest.testWrapping(ChiliLogExceptionTest.java:69)\n"
                                + "at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n"
                                + "at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)\n"
                                + "at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)\n"
                                + "at java.lang.reflect.Method.invoke(Method.java:597)\n"
                                + "at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:44)\n"
                                + "at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:15)\n"
                                + "at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:41)\n"
                                + "at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:20)\n"
                                + "at org.junit.runners.BlockJUnit4ClassRunner.runNotIgnored(BlockJUnit4ClassRunner.java:79)\n"
                                + "at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:71)\n"
                                + "at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:49)\n"
                                + "at org.junit.runners.ParentRunner$3.run(ParentRunner.java:193)\n"
                                + "at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:52)\n"
                                + "at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:191)\n"
                                + "at org.junit.runners.ParentRunner.access$000(ParentRunner.java:42)\n"
                                + "at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:184)\n"
                                + "at org.junit.runners.ParentRunner.run(ParentRunner.java:236)\n"
                                + "at org.eclipse.jdt.internal.junit4.runner.JUnit4TestReference.run(JUnit4TestReference.java:49)\n"
                                + "at org.eclipse.jdt.internal.junit.runner.TestExecution.run(TestExecution.java:38)\n"
                                + "at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:467)\n"
                                + "at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:683)\n"
                                + "at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.run(RemoteTestRunner.java:390)\n"
                                + "at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.main(RemoteTestRunner.java:197)\n"
                                + "Caused by: java.lang.NullPointerException: inner exception\n"
                                + "at com.chililog.server.common.ChiliLogExceptionTest.testWrapping(ChiliLogExceptionTest.java:63)\n"
                                + "... 23 more", -1);
        _logger.info(stackTrace.toString());

        return;
    }

    /**
     * On my machine, brute force search is better than hash if there are 8 item or less. Since most text will have more
     * than 8 terms, we will use hash map.
     * 
     * @throws IOException
     */
    @Test
    public void testBenchmarkHashMap() throws IOException {
        ArrayList<String> l = new ArrayList<String>();
        l.add("the");
        l.add("quick");
        l.add("brown");
        l.add("fox");
        l.add("jumped");
        l.add("over");
        l.add("lazy");
        l.add("dog");
        // l.add("this");
        // l.add("is");
        // l.add("testing");
        // l.add("which");
        // l.add("search");
        // l.add("method");
        // l.add("faster");

        HashMap<String, String> m = new HashMap<String, String>();
        for (String s : l) {
            m.put(s, s);
        }

        Date startTime = new Date();
        for (int i = 0; i < 1000000; i++) {
            l.contains("faster");
        }
        Date endTime = new Date();
        _logger.info("Array search: %s", endTime.getTime() - startTime.getTime());

        startTime = new Date();
        for (int i = 0; i < 1000000; i++) {
            m.containsKey("faster");
        }
        endTime = new Date();
        _logger.info("Hash search: %s", endTime.getTime() - startTime.getTime());

    }

    /**
     * Regular expression is slow
     * 
     * @throws IOException
     */
    @Test
    public void testBenchmarkRegex() throws IOException {
        Date startTime = new Date();
        for (int i = 0; i < 10000; i++) {
            basicTokenize("2011-03-26 15:32:22,376 [main] ERROR com.chililog.server.common.ChiliLogExceptionTest - "
                    + "com.chililog.server.common.ChiliLogException: Test12\n"
                    + "at com.chililog.server.common.ChiliLogExceptionTest.testWrapping(ChiliLogExceptionTest.java:69)\n"
                    + "at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n"
                    + "at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)\n"
                    + "at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)\n"
                    + "at java.lang.reflect.Method.invoke(Method.java:597)\n"
                    + "at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:44)\n"
                    + "at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:15)\n"
                    + "at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:41)\n"
                    + "at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:20)\n"
                    + "at org.junit.runners.BlockJUnit4ClassRunner.runNotIgnored(BlockJUnit4ClassRunner.java:79)\n"
                    + "at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:71)\n"
                    + "at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:49)\n"
                    + "at org.junit.runners.ParentRunner$3.run(ParentRunner.java:193)\n"
                    + "at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:52)\n"
                    + "at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:191)\n"
                    + "at org.junit.runners.ParentRunner.access$000(ParentRunner.java:42)\n"
                    + "at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:184)\n"
                    + "at org.junit.runners.ParentRunner.run(ParentRunner.java:236)\n"
                    + "at org.eclipse.jdt.internal.junit4.runner.JUnit4TestReference.run(JUnit4TestReference.java:49)\n"
                    + "at org.eclipse.jdt.internal.junit.runner.TestExecution.run(TestExecution.java:38)\n"
                    + "at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:467)\n"
                    + "at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:683)\n"
                    + "at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.run(RemoteTestRunner.java:390)\n"
                    + "at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.main(RemoteTestRunner.java:197)\n"
                    + "Caused by: java.lang.NullPointerException: inner exception\n"
                    + "at com.chililog.server.common.ChiliLogExceptionTest.testWrapping(ChiliLogExceptionTest.java:63)\n"
                    + "... 23 more");

        }
        Date endTime = new Date();
        _logger.info("No special parsing search: %s", endTime.getTime() - startTime.getTime());

        startTime = new Date();
        for (int i = 0; i < 10000; i++) {
            TextTokenizer
                    .getInstance()
                    .tokenize(
                            "2011-03-26 15:32:22,376 [main] ERROR com.chililog.server.common.ChiliLogExceptionTest - "
                                    + "com.chililog.server.common.ChiliLogException: Test12\n"
                                    + "at com.chililog.server.common.ChiliLogExceptionTest.testWrapping(ChiliLogExceptionTest.java:69)\n"
                                    + "at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n"
                                    + "at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)\n"
                                    + "at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)\n"
                                    + "at java.lang.reflect.Method.invoke(Method.java:597)\n"
                                    + "at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:44)\n"
                                    + "at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:15)\n"
                                    + "at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:41)\n"
                                    + "at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:20)\n"
                                    + "at org.junit.runners.BlockJUnit4ClassRunner.runNotIgnored(BlockJUnit4ClassRunner.java:79)\n"
                                    + "at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:71)\n"
                                    + "at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:49)\n"
                                    + "at org.junit.runners.ParentRunner$3.run(ParentRunner.java:193)\n"
                                    + "at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:52)\n"
                                    + "at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:191)\n"
                                    + "at org.junit.runners.ParentRunner.access$000(ParentRunner.java:42)\n"
                                    + "at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:184)\n"
                                    + "at org.junit.runners.ParentRunner.run(ParentRunner.java:236)\n"
                                    + "at org.eclipse.jdt.internal.junit4.runner.JUnit4TestReference.run(JUnit4TestReference.java:49)\n"
                                    + "at org.eclipse.jdt.internal.junit.runner.TestExecution.run(TestExecution.java:38)\n"
                                    + "at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:467)\n"
                                    + "at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:683)\n"
                                    + "at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.run(RemoteTestRunner.java:390)\n"
                                    + "at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.main(RemoteTestRunner.java:197)\n"
                                    + "Caused by: java.lang.NullPointerException: inner exception\n"
                                    + "at com.chililog.server.common.ChiliLogExceptionTest.testWrapping(ChiliLogExceptionTest.java:63)\n"
                                    + "... 23 more", -1);

        }
        endTime = new Date();
        _logger.info("Hardcoded parsing search: %s", endTime.getTime() - startTime.getTime());

        /**
         * Thanks to http://fightingforalostcause.net/misc/2006/compare-email-regex.php
         */
        Pattern EMAIL_ADDRESS_PATTERN = Pattern.compile("^(.)+@[.]+$");

        Pattern CLASS_NAME_PATTERN = Pattern.compile("^(\\D.*)\\.(\\D.*)*$");
        startTime = new Date();
        for (int i = 0; i < 10000; i++) {
            List<String> l = basicTokenize("2011-03-26 15:32:22,376 [main] ERROR com.chililog.server.common.ChiliLogExceptionTest - "
                    + "com.chililog.server.common.ChiliLogException: Test12\n"
                    + "at com.chililog.server.common.ChiliLogExceptionTest.testWrapping(ChiliLogExceptionTest.java:69)\n"
                    + "at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n"
                    + "at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)\n"
                    + "at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)\n"
                    + "at java.lang.reflect.Method.invoke(Method.java:597)\n"
                    + "at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:44)\n"
                    + "at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:15)\n"
                    + "at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:41)\n"
                    + "at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:20)\n"
                    + "at org.junit.runners.BlockJUnit4ClassRunner.runNotIgnored(BlockJUnit4ClassRunner.java:79)\n"
                    + "at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:71)\n"
                    + "at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:49)\n"
                    + "at org.junit.runners.ParentRunner$3.run(ParentRunner.java:193)\n"
                    + "at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:52)\n"
                    + "at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:191)\n"
                    + "at org.junit.runners.ParentRunner.access$000(ParentRunner.java:42)\n"
                    + "at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:184)\n"
                    + "at org.junit.runners.ParentRunner.run(ParentRunner.java:236)\n"
                    + "at org.eclipse.jdt.internal.junit4.runner.JUnit4TestReference.run(JUnit4TestReference.java:49)\n"
                    + "at org.eclipse.jdt.internal.junit.runner.TestExecution.run(TestExecution.java:38)\n"
                    + "at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:467)\n"
                    + "at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:683)\n"
                    + "at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.run(RemoteTestRunner.java:390)\n"
                    + "at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.main(RemoteTestRunner.java:197)\n"
                    + "Caused by: java.lang.NullPointerException: inner exception\n"
                    + "at com.chililog.server.common.ChiliLogExceptionTest.testWrapping(ChiliLogExceptionTest.java:63)\n"
                    + "... 23 more");

            for (String term : l) {
                CLASS_NAME_PATTERN.matcher(term).matches();
                EMAIL_ADDRESS_PATTERN.matcher(term).matches();
                CLASS_NAME_PATTERN.matcher(term).matches();
                EMAIL_ADDRESS_PATTERN.matcher(term).matches();
            }
        }
        endTime = new Date();
        _logger.info("With Regex search: %s", endTime.getTime() - startTime.getTime());

    }

    /**
     * Used for benchmarking ... basic tokenizing without regular expression
     * 
     * @param text
     * @return
     * @throws IOException
     */
    public List<String> basicTokenize(String text) throws IOException {
        List<String> tokens = new ArrayList<String>();

        if (StringUtils.isEmpty(text)) {
            return tokens;
        }

        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
        HashMap<String, String> lookup = new HashMap<String, String>();
        TokenStream stream = analyzer.tokenStream("field", new StringReader(text));

        TermAttribute termAttribute = stream.getAttribute(TermAttribute.class);
        while (stream.incrementToken()) {
            String term = termAttribute.term();
            if (!lookup.containsKey(term)) {
                tokens.add(term);
                lookup.put(term, null);
            }
        }

        return tokens;
    }
}
