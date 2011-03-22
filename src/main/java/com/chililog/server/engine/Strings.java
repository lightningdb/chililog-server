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

package com.chililog.server.engine;

/**
 * Codes for strings in the strings.properties file
 * 
 * @author vibul
 * 
 */
public class Strings
{
    public static final String PREFIX = "Engine.";
    public static final String START_REPOSITORY_ERROR = PREFIX + "StartRepositoryError";
    public static final String REPOSITORY_ALREADY_STARTED_ERROR = PREFIX + "RepositoryAlreadyStartedError";
    public static final String REPOSITORY_INFO_UPDATE_ERROR = PREFIX + "RepositoryInfoUpdateError";
    public static final String STOP_REPOSITORY_ERROR = PREFIX + "StopRepositoryError";
    public static final String START_REPOSITORY_QUEUE_ERROR = PREFIX + "StartRepositoryQueueError";
    public static final String STOP_REPOSITORY_QUEUE_ERROR = PREFIX + "StopRepositoryQueueError";
    public static final String START_REPOSITORY_WRITERS_ERROR = PREFIX + "StartRepositoryWritersError";
    public static final String STOP_REPOSITORY_WRITERS_ERROR = PREFIX + "StopRepositoryWritersError";
    public static final String LOAD_REPOSITORIES_ERROR = PREFIX + "LoadRepositoriesError";
    public static final String CLOSE_MQ_SESSION_ERROR = PREFIX + "CloseMqSessionError";
    
    public static final String PARSER_FACTORY_ERROR = PREFIX + "Parser.FactoryError";
    public static final String PARSER_INITIALIZATION_ERROR = PREFIX + "Parser.IntializationError";
    public static final String PARSER_DELIMITER_NOT_SET_ERROR = PREFIX + "Parser.DelimiterNotSetError";
    public static final String PARSER_FIELD_ERROR_SKIP_FIELD = PREFIX + "Parser.FieldErrorSkipField";
    public static final String PARSER_FIELD_ERROR_SKIP_ENTRY = PREFIX + "Parser.FieldErrorSkipEntry";
    public static final String PARSER_JSON_ERROR_SKIP_ENTRY = PREFIX + "Parser.JsonErrorSkipEntry";
    public static final String PARSER_BLANK_ERROR = PREFIX + "Parser.BlankError";
}
