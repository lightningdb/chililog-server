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

package com.chililog.server.common;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.Version;

public class TextTokenizer
{
    /**
     * Returns the singleton instance for this class
     */
    public static TextTokenizer getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    /**
     * SingletonHolder is loaded on the first execution of Singleton.getInstance() or the first access to
     * SingletonHolder.INSTANCE, not before.
     * 
     * @see http://en.wikipedia.org/wiki/Singleton_pattern
     */
    private static class SingletonHolder
    {
        public static final TextTokenizer INSTANCE = new TextTokenizer();
    }

    /**
     * 
     */
    private TextTokenizer()
    {

    }

    /**
     * <p>
     * Tokenizes text to get keywords
     * </p>
     * <p>
     * We use lucene <code>StandardAnalyzer</code> with a bit of spice. We want to break up domain names, class names
     * and emails so we have to do some extra parsing.
     * </p>
     * <p>
     * Lucene parsing:
     * <ul>
     * <li>"email@address.com" = ["email@address", "com"]</li>
     * <li>"com.chililog.server.common.ChiliLogExceptionTest" = ["com.chililog.server.common", "chililogexceptiontest"]</li>
     * </ul>
     * </p>
     * <p>
     * We have not used regular expression because it is slow. We have implemented this as a singleton so that in the
     * future we can allow user customization.
     * </p>
     * 
     * @param text
     *            Text to extract keywords
     * @return Array of keywords
     * @throws IOException
     */
    public List<String> tokenize(String text) throws IOException
    {
        List<String> tokens = new ArrayList<String>();

        if (StringUtils.isEmpty(text))
        {
            return tokens;
        }
        
        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
        HashMap<String, String> lookup = new HashMap<String, String>();
        TokenStream stream = analyzer.tokenStream("field", new StringReader(text));

        StringBuilder sb = new StringBuilder();
        TermAttribute termAttribute = stream.getAttribute(TermAttribute.class);
        while (stream.incrementToken())
        {
            char[] termBuffer = termAttribute.termBuffer();
            int length = termAttribute.termLength();

            boolean doSplit = true;

            // Check if we want to split
            if (Character.isDigit(termBuffer[0]))
            {
                doSplit = false;
            }
            else
            {
                for (int j = 0; j < length; j++)
                {
                    char c = termBuffer[j];
                    if (!Character.isLetterOrDigit(c) && c != '.' && c != '@')
                    {
                        doSplit = false;
                        break;
                    }
                }
            }

            if (doSplit)
            {
                sb.setLength(0);
                for (int i = 0; i < length; i++)
                {
                    char c = termBuffer[i];
                    if (c == '.' || c == '@')
                    {
                        String term = sb.toString();
                        if (sb.length() > 0 && !lookup.containsKey(term))
                        {
                            tokens.add(term);
                            lookup.put(term, null);
                        }
                        sb.setLength(0);
                    }
                    else
                    {
                        sb.append(c);
                    }
                }

                // last part
                String term = sb.toString();
                if (sb.length() > 0 && !lookup.containsKey(term))
                {
                    tokens.add(term);
                    lookup.put(term, null);
                }
            }
            else
            {
                // No splitting, just add term
                String term = termAttribute.term();
                if (!lookup.containsKey(term))
                {
                    tokens.add(term);
                    lookup.put(term, null);
                }
            }
        }

        return tokens;
    }
}
