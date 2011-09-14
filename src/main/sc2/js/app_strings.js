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

//
// Strings table. By convention, all string  id have an underscore (_) prefix so that
// we know immediately if we display the code or the string.
//

SC.STRINGS = {
 // ********************************************************
  // General
  // ********************************************************
  '_add': 'Add',
  '_save': 'Save',
  '_cancel': 'Cancel',
  '_refresh': 'Refresh',
  '_refreshing': 'Refreshing ...',
  '_delete': 'Delete',
  '_deleteSelected': 'Delete Selected',
  '_back': '\< Back',
  '_moreActions': 'More Actions',
  '_saveSuccess': 'Changes successfully saved',
  '_start': 'Start',
  '_starting': 'Starting ...',
  '_stop': 'Stop',
  '_stopping': 'Stopping ...',
  '_clear': 'Clear',
  '_search': 'Search',
  '_showMore': 'Show More',
  '_done': 'Done',
  '_next': '>',
  '_nextTooltip': 'Next',
  '_previous': '\<',
  '_previousTooltip': 'Previous',

  '_thousandSeparator': ',',

  '_testError': 'Test Error param1=%@, param2=%@, param3=%@',

  // ********************************************************
  // Engines
  // ********************************************************
  '_documentIDError': 'Expected document id "%@" but received "%@" from server.',

  '_sessionEngine.TokenNotFoundInResponseError': 'Token not found in authentication response',
  '_sessionEngine.VersionNotFoundInResponseError': 'Version number not found in authentication response',
  '_sessionEngine.BuildTimestampNotFoundInResponseError': 'Build timestamp not found in authentication response',

  // ********************************************************
  // Records
  // ********************************************************
  '_repositoryEntryRecord.Severity.Emergency': 'Emergency',
  '_repositoryEntryRecord.Severity.Action': 'Action',
  '_repositoryEntryRecord.Severity.Critical': 'Critical',
  '_repositoryEntryRecord.Severity.Error': 'Error',
  '_repositoryEntryRecord.Severity.Warning': 'Warning',
  '_repositoryEntryRecord.Severity.Notice': 'Notice',
  '_repositoryEntryRecord.Severity.Information': 'Information',
  '_repositoryEntryRecord.Severity.Debug': 'Debug',
  
  '_repositoryStatusRecord.Status.Online': 'Online',
  '_repositoryStatusRecord.Status.Offline': 'Offline',

  // ********************************************************
  // Login Page
  // ********************************************************
  '_login.username': 'Username',
  '_login.username.required': 'Username is required',
  '_login.password': 'Password',
  '_login.password.required': 'Password is required',
  '_login.rememberMe': 'Remember Me',
  '_login.login': 'Login',

  // ********************************************************
  // Stream Page
  // ********************************************************
  '_stream.repository': 'Repository',
  '_stream.severity': 'Severity',
  '_stream.test': 'Send Test Log Entries',

  // ********************************************************
  // Search Page
  // ********************************************************
  '_search.repository': 'Search In Repository',
  '_search.timespan': 'In The Past',
  '_search.timespan.5': '5 minutes',
  '_search.timespan.15': '15 minutes',
  '_search.timespan.30': '30 minutes',
  '_search.timespan.60': '60 minutes',
  '_search.timespan.1440': '24 hours',
  '_search.timespan.10080': '7 days',
  '_search.timespan.20160': '14 days',
  '_search.timespan.43200': '30 days',
  '_search.fromDate': 'From Date',
  '_search.fromDate.invalid': 'Cannot recognise From Date value "%@". Please use the format "yyyy-mm-dd".',
  '_search.fromTime': 'From Time',
  '_search.fromTime.invalid': 'Cannot recognise From Time time value "%@". Please use the format "hh:mm:ss".',
  '_search.toDate': 'To Date',
  '_search.toDate.invalid': 'Cannot recognise From Date value "%@". Please use the format "yyyy-mm-dd".',
  '_search.toTime': 'To Time',
  '_search.toTime.invalid': 'Cannot recognise From Time time value "%@". Please use the format "hh:mm:ss".',
  '_search.keywords': 'Keywords',
  '_search.timestamp': 'Timestamp',
  '_search.source': 'Source',
  '_search.host': 'Host',
  '_search.severity': 'Severity',
  '_search.message': 'Message',
  '_search.fields': 'Fields',
  '_search.savedTimestamp': 'Saved Timestamp',
  '_search.documentID': 'ID',
  '_search.advancedCriteria': 'Advanced Options',
  '_search.condition': 'Conditions',
  '_search.condition.help': '(mongodb JSON format must be used.)',
  '_search.condition.invalid': 'Conditions must be in valid JSON format. Check mongodb script for examples.',
  '_search.showMore': 'Show More Rows',
  '_search.timeSpecifiedError': 'You have specified "In The Past" and a specific date and time. Please specify one or the other but not both.',
  '_search.dateTimeRangeError': 'From date and time is greater than To date and time.',
  '_search.logEntries': 'Log Entries',
  '_search.noRowsFound': 'No log entries found matching your selection criteria',

  // ********************************************************
  // My Profile
  // ********************************************************
  '_myAccount.Title': 'My Account',
  '_myAccount.changeProfile': 'Change Profile',
  '_myAccount.username': 'Username',
  '_myAccount.emailAddress': 'Email Address',
  '_myAccount.emailAddress.invalid': 'Email Address is invalid',
  '_myAccount.emailAddress.required': 'Email Address is required',
  '_myAccount.displayName': 'Display Name',
  '_myAccount.changePassword': 'Change My Password',
  '_myAccount.oldPassword': 'Current Password',
  '_myAccount.oldPassword.invalid': 'Invalid password. Make sure that you have correctly entered your current password.',
  '_myAccount.oldPassword.required': 'Current Password is required.',
  '_myAccount.newPassword': 'New Password',
  '_myAccount.newPassword.invalid': 'You password must be at least 8 characters long.',
  '_myAccount.newPassword.required': 'New Password is required.',
  '_myAccount.confirmPassword': 'Confirm Password',
  '_myAccount.confirmPassword.help': 'Re-enter you new password to confirm that you have typed it correctly',
  '_myAccount.confirmPassword.invalid': 'New Password and Confirm Password are not the same.',
  '_myAccount.confirmPassword.required': 'Confirm New Password is required.',
  '_myAccount.changePassword.success': 'Password successfully changed',
  
  // ********************************************************
  // Admin
  // ********************************************************
  '_admin.repo.name': 'Repository Name',

  '_admin.user.username': 'Username',
  '_admin.user.username.invalid': 'Username is invalid. Username cannot have spaces.',
  '_admin.user.username.required': 'Username is required.',
  '_admin.user.emailAddress': 'Email Address',
  '_admin.user.emailAddress.invalid': 'Email Address "%@" is invalid.',
  '_admin.user.emailAddress.required': 'Email Address is required.',
  '_admin.user.displayName': 'Display Name',
  '_admin.user.displayName.help': 'Name or nickname to display instead of the username',
  '_admin.user.currentStatus': 'Status',
  '_admin.user.currentStatus.enabled': 'Enabled <span class="optionHelp">User can login.</span>',
  '_admin.user.currentStatus.disabled': 'Disabled <span class="optionHelp">User cannot login.</span>',
  '_admin.user.currentStatus.locked': 'Locked <span class="optionHelp">User failed to login too many times. Password must be reset.</span>',
  '_admin.user.password': 'New Password',
  '_admin.user.password.invalid': 'You password must be at least 8 characters long.',
  '_admin.user.password.required': 'New Password is required.',
  '_admin.user.confirmPassword': 'Confirm Password',
  '_admin.user.confirmPassword.help': 'Re-enter you new password to confirm that you have typed it correctly',
  '_admin.user.confirmPassword.invalid': 'New Password and Confirm Password are not the same.',
  '_admin.user.confirmPassword.required': 'Confirm New Password is required.',
  '_admin.user.isSystemAdministrator': 'Is System Administrator?',
  '_admin.user.isSystemAdministrator.Yes': 'Yes. <span class="help">User will have access to all repositories and users.</span>',
  '_admin.user.isSystemAdministrator.No': 'No.',
  '_admin.user.repositoryAccesses': 'Repository Access',
  '_admin.user.repositoryAccesses.repository': 'Repository',
  '_admin.user.repositoryAccesses.role': 'Role',
  '_admin.user.repositoryAccesses.adminRole': 'Administrator',
  '_admin.user.repositoryAccesses.workbenchRole': 'Workbench User',
  '_admin.user.repositoryAccesses.publisherRole': 'Publisher',
  '_admin.user.repositoryAccesses.subscriberRole': 'Subscriber',
  '_admin.user.repositoryAccesses.alreadyExists': 'Access to "%@" repository as "%@" has already been granted.',
  '_admin.user.noRowsFound': 'No users found matching your search criteria.',
  '_admin.user.createTitle': 'New User',
  '_admin.user.editTitle': 'User: %@',
  '_admin.user.generalAttributes': 'General',
  '_admin.user.rolesAttributes': 'Roles',
  '_admin.user.confirmDelete': 'Delete user "%@"?',

  // ********************************************************
  // OLD STUFF to be converted
  // ********************************************************
  '_configureRepositoryInfoListView.Title': 'Configure Repositories',
  '_configureRepositoryInfoListView.Create': 'Create a New Repository',
  '_configureRepositoryInfoDetailView.EditTitle': 'Repository: %@',
  '_configureRepositoryInfoDetailView.CreateTitle': 'New Repository',
  '_configureRepositoryInfoDetailView.Title': 'Repository',
  '_configureRepositoryInfoDetailView.Name': 'Name',
  '_configureRepositoryInfoDetailView.Name.Help': 'Only lower case letters (a-z), digits (0-9) and underscore (_) characters allowed.',
  '_configureRepositoryInfoDetailView.Name.Invalid': 'Repository Name "%a" contains invalid characters. Only lower case letters (a-z), digits (0-9) and underscore (_) characters allowed.',
  '_configureRepositoryInfoDetailView.Name.Required': 'Repository Name is required.',
  '_configureRepositoryInfoDetailView.DisplayName': 'Display Name',
  '_configureRepositoryInfoDetailView.Description': 'Description',
  '_configureRepositoryInfoDetailView.StartupStatus': 'Startup Status',
  '_configureRepositoryInfoDetailView.StartupStatus.Online': 'Online. <span class="help">Start repository when server starts.</span>',
  '_configureRepositoryInfoDetailView.StartupStatus.Offline': 'Offline. <span class="help">Repository is not started. You must manually start the repository after the server starts.</span>',
  '_configureRepositoryInfoDetailView.CurrentStatus': 'Current Status',
  '_configureRepositoryInfoDetailView.StoreEntries' : 'Store Log Entries?',
  '_configureRepositoryInfoDetailView.StoreEntries.Yes' : 'Yes. <span class="help">Save log entries to the database so they can be searched at a later time.</span>',
  '_configureRepositoryInfoDetailView.StoreEntries.No' : 'No. <span class="help">Do not save log entries. Entries cannot be search.</span>',
  '_configureRepositoryInfoDetailView.StorageQueueDurable' : 'Storage Queue Durable?',
  '_configureRepositoryInfoDetailView.StorageQueueDurable.Yes' : 'Yes. <span class="help">Queued data saved to disk to prevent loss of data if server goes down.</span>',
  '_configureRepositoryInfoDetailView.StorageQueueDurable.No' : 'No. <span class="help">Queued data not saved to disk to improve throughput.</span>',
  '_configureRepositoryInfoDetailView.StorageQueueWorkerCount' : 'Worker Thread Count',
  '_configureRepositoryInfoDetailView.StorageQueueWorkerCount.Help' : 'Number of worker threads that writes log entries to the database. More workers means faster storage of entries and smaller queue size. However, more workers results in higher CPU and RAM utilization.',
  '_configureRepositoryInfoDetailView.StorageQueueWorkerCount.Required' : 'Worker Thread Count required.',
  '_configureRepositoryInfoDetailView.StorageMaxKeywords' : 'Maximum Keywords',
  '_configureRepositoryInfoDetailView.StorageMaxKeywords.Help' : 'Maximum number of keywords per log entry to save. The more keywords, the more memory that is used.',
  '_configureRepositoryInfoDetailView.StorageMaxKeywords.Required' : 'Maximum Keywords is required.',
  '_configureRepositoryInfoDetailView.MaxMemory' : 'Maximum Memory (bytes)',
  '_configureRepositoryInfoDetailView.MaxMemory.Help' : 'Maximum amount of memory that will be used to queue log entries.',
  '_configureRepositoryInfoDetailView.MaxMemory.Required' : 'Maximum Memory that will be used to queue log entries is required.',
  '_configureRepositoryInfoDetailView.MaxMemoryPolicy' : 'Maximum Memory Policy',
  '_configureRepositoryInfoDetailView.MaxMemoryPolicy.Page' : 'Page. <span class="help">When maximum memory is reached, new messages will be saved into page files.</span>',
  '_configureRepositoryInfoDetailView.MaxMemoryPolicy.Drop' : 'Drop. <span class="help">When maximum memory is reached, new messages will be dropped and not processed.</span>',
  '_configureRepositoryInfoDetailView.MaxMemoryPolicy.Block' : 'Block. <span class="help">When maximum memory is reached, force producers to wait before new messages can be sent.</span>',
  '_configureRepositoryInfoDetailView.PageSize' : 'Page File Size',
  '_configureRepositoryInfoDetailView.PageSize.Help' : 'The size of each page file. Only applicable for Paging Mode.',
  '_configureRepositoryInfoDetailView.PageSize.Required' : 'Page File Size is required.',
  '_configureRepositoryInfoDetailView.PageSize.InvalidSize' : 'Page File Size (%@) must be less than Maximum Memory (%@).',
  '_configureRepositoryInfoDetailView.PageCountCache' : 'Page File Cache',
  '_configureRepositoryInfoDetailView.PageCountCache.Help' : 'Number of page files to keep in memory. The more files, the faster the performance.',
  '_configureRepositoryInfoDetailView.PageCountCache.Required' : 'Page File Cache is required.',
  '_configureRepositoryInfoDetailView.Status.Online': 'Online',
  '_configureRepositoryInfoDetailView.Status.Offline': 'Offline',
  '_configureRepositoryInfoDetailView.ConfirmDelete': 'Delete repository "%@"?',
  '_configureRepositoryInfoDetailView.GeneralAttributes': 'General',
  '_configureRepositoryInfoDetailView.PubSubAttributes': 'Publication & Subscription',
  '_configureRepositoryInfoDetailView.StorageAttributes': 'Storage',
  '_configureRepositoryInfoDetailView.RepositoryAccesses': 'Users',
  '_configureRepositoryInfoDetailView.RepositoryAccesses.Label': 'Users who can access this repository',
  '_configureRepositoryInfoDetailView.RepositoryAccesses.Username': 'Username',
  '_configureRepositoryInfoDetailView.RepositoryAccesses.UserDisplayName': 'User Display Name',
  '_configureRepositoryInfoDetailView.RepositoryAccesses.Role': 'Role',

  'end': 'end'
};