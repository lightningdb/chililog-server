// ==========================================================================
// Project:   Project Name Strings
// Copyright: ©2009 My Company, Inc.
// ==========================================================================
/*globals Project Name */
// Place strings you want to localize here.  In your app, use the key and
// localize it using "key string".loc().  HINT: For your key names, use the
// english string with an underscore in front.  This way you can still see
// how your UI will look and you'll notice right away when something needs a
// localized string added to this file!
//

SC.stringsFor('English', {
  // ********************************************************
  // General
  // ********************************************************
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
  '_showMore': 'Show More',
  '_done': 'Done',
  '_next': '>',
  '_previous': '\<',

  '_thousandSeparator': ',',

  '_testError': 'Test Error param1=%@, param2=%@, param3=%@',

  // ********************************************************
  // Views
  // ********************************************************
  '_mainPane.Search': 'Search',
  '_mainPane.Search.ToolTip': 'Search for log entries',
  '_mainPane.Analyse': 'Analyse',
  '_mainPane.Analyse.ToolTip': 'Use map/reduce to count and group log entries',
  '_mainPane.Monitor': 'Monitors',
  '_mainPane.Monitor.ToolTip': 'Setup monitors to notify you when patterns in log entries are matched',
  '_mainPane.Configure': 'Configure',
  '_mainPane.Configure.ToolTip': 'Manage your repositories and users',
  '_mainPane.About': 'About',
  '_mainPane.About.ToolTip': 'Information about ChiliLog and how to contact us',
  '_mainPane.MyProfile': 'Profile',
  '_mainPane.MyProfile.ToolTip': 'Change your account information',
  '_mainPane.Logout': 'Logout',

  '_loginPane.Username': 'Username',
  '_loginPane.Username.Required': 'Username is required',
  '_loginPane.Password': 'Password',
  '_loginPane.Password.Required': 'Password is required',
  '_loginPane.RememberMe': 'Remember Me',
  '_loginPane.Login': 'Login',

  '_myAccountView.Title': 'My Account',
  '_myAccountView.MyProfile': 'My Profile',
  '_myAccountView.ChangePassword': 'Change Password',
  '_myAccountMyProfileView.Username': 'Username',
  '_myAccountMyProfileView.Username.Invalid': 'Username is invalid',
  '_myAccountMyProfileView.Username.Required': 'Username is requried',
  '_myAccountMyProfileView.EmailAddress': 'Email Address',
  '_myAccountMyProfileView.EmailAddress.Invalid': 'Email Address is invalid',
  '_myAccountMyProfileView.EmailAddress.Required': 'Email Address is required',
  '_myAccountMyProfileView.DisplayName': 'Display Name',
  '_myAccountMyProfileView.DisplayName.Help': '(Optional name or nickname to display instead of the username)',
  '_myAccountMyPasswordView.ChangePassword': 'Change My Password',
  '_myAccountMyPasswordView.OldPassword': 'Old Password',
  '_myAccountMyPasswordView.OldPassword.Required': 'Old Password is required.',
  '_myAccountMyPasswordView.NewPassword': 'New Password',
  '_myAccountMyPasswordView.NewPassword.Help': 'Must be at least 8 characters long and contains mixed case letters, numbers and a punctuation character like "!" or "#".',
  '_myAccountMyPasswordView.NewPassword.Invalid': 'You password must be at least 8 characters long and contains mixed case letters, numbers and a punctuation character like "!" or "#".',
  '_myAccountMyPasswordView.NewPassword.Required': 'New Password is required.',
  '_myAccountMyPasswordView.ConfirmNewPassword': 'Confirm New Password',
  '_myAccountMyPasswordView.ConfirmNewPassword.Invalid': 'New Password and Confirm New Passwords are not the same. Please re-enter you new password again.',
  '_myAccountMyPasswordView.ConfirmNewPassword.Required': 'Confirm New Password is required.',
  '_myAccountMyPasswordView.ChangePassword.Success': 'Password changed',

  '_configureView.Title': 'Configure',
  '_configureView.Repositories': 'Repositories',
  '_configureView.Users': 'Users',
  '_configureView.NewRepository': 'New Repository',
  '_configureView.NewUser': 'New User',
  '_configureUserListView.Title': 'Users',
  '_configureUserListView.Create': 'Create a New User',
  '_configureUserDetailView.EditTitle': 'User: %@',
  '_configureUserDetailView.CreateTitle': 'New User',
  '_configureUserDetailView.Username': 'Username',
  '_configureUserDetailView.Username.Invalid': 'Username is invalid. Username cannot have spaces.',
  '_configureUserDetailView.Username.Required': 'Username is required.',
  '_configureUserDetailView.EmailAddress': 'Email Address',
  '_configureUserDetailView.EmailAddress.Invalid': 'Email Address "%@" is invalid.',
  '_configureUserDetailView.EmailAddress.Required': 'Email Address is required.',
  '_configureUserDetailView.DisplayName': 'Display Name',
  '_configureUserDetailView.DisplayName.Help': '(Optional name or nickname to display instead of the username)',
  '_configureUserDetailView.CurrentStatus': 'Status',
  '_configureUserDetailView.CurrentStatus.Enabled': 'Enabled. <span class="help">User can login.</span>',
  '_configureUserDetailView.CurrentStatus.Disabled': 'Disabled. <span class="help">User cannot login.</span>',
  '_configureUserDetailView.CurrentStatus.Locked': 'Locked. <span class="help">User failed to login too many times. Password must be reset.</span>',
  '_configureUserDetailView.Password': 'Password',
  '_configureUserDetailView.Password.Help': 'Must be at least 8 characters long and contains mixed case letters, numbers and a punctuation character like "!" or "#".',
  '_configureUserDetailView.Password.Invalid': 'You password must be at least 8 characters long and contains mixed case letters, numbers and a punctuation character like "!" or "#".',
  '_configureUserDetailView.Password.Required': 'Password is required',
  '_configureUserDetailView.ConfirmPassword': 'Confirm Password',
  '_configureUserDetailView.ConfirmPassword.Invalid': 'New Password and Confirm New Passwords are not the same. Please re-enter you new password again.',
  '_configureUserDetailView.ConfirmPassword.Required': 'Confirm Password is required',
  '_configureUserDetailView.ConfirmDelete': 'Delete user "%@"?',
  '_configureRepositoryInfoListView.Title': 'Repositories',
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
  '_configureRepositoryInfoDetailView.WriteDivider': 'Write Queue Attributes',
  '_configureRepositoryInfoDetailView.WriteQueueAddress' : 'Address',
  '_configureRepositoryInfoDetailView.WriteQueueUsername' : 'Login Username',
  '_configureRepositoryInfoDetailView.WriteQueuePassword' : 'Login Password',
  '_configureRepositoryInfoDetailView.WriteQueueDurable' : 'Durable?',
  '_configureRepositoryInfoDetailView.WriteQueueDurable.Yes' : 'Yes. <span class="help">Queued data saved to disk to prevent loss of data if server goes down.</span>',
  '_configureRepositoryInfoDetailView.WriteQueueDurable.No' : 'No. <span class="help">Queued data not saved to disk to improve throughput.</span>',
  '_configureRepositoryInfoDetailView.WriteQueueWorkerCount' : 'Worker Thread Count',
  '_configureRepositoryInfoDetailView.WriteQueueWorkerCount.Help' : 'Number of worker threads that will consume log entries from this queue and write them to the database.',
  '_configureRepositoryInfoDetailView.WriteQueueWorkerCount.Required' : 'Worker Thread Count required.',
  '_configureRepositoryInfoDetailView.WriteQueueMaxMemory' : 'Maximum Memory (bytes)',
  '_configureRepositoryInfoDetailView.WriteQueueMaxMemory.Help' : 'Maximum memory that will be used to store queued items.',
  '_configureRepositoryInfoDetailView.WriteQueueMaxMemory.Required' : 'Maximum Memory that will be used to store queued items is required.',
  '_configureRepositoryInfoDetailView.WriteQueueMaxMemoryPolicy' : 'Maximum Memory Policy',
  '_configureRepositoryInfoDetailView.WriteQueueMaxMemoryPolicy.Page' : 'Page. <span class="help">When maximum memory is reached, new messages will be saved into page files.</span>',
  '_configureRepositoryInfoDetailView.WriteQueueMaxMemoryPolicy.Drop' : 'Drop. <span class="help">When maximum memory is reached, new messages will be dropped and not processed.</span>',
  '_configureRepositoryInfoDetailView.WriteQueueMaxMemoryPolicy.Block' : 'Block. <span class="help">When maximum memory is reached, force producers to wait before new messages can be sent.</span>',
  '_configureRepositoryInfoDetailView.WriteQueuePageSize' : 'Page File Size',
  '_configureRepositoryInfoDetailView.WriteQueuePageSize.Help' : 'The size of each page file. Only applicable for Paging Mode.',
  '_configureRepositoryInfoDetailView.WriteQueuePageSize.Required' : 'Page File Size is required.',
  '_configureRepositoryInfoDetailView.WriteQueuePageSize.InvalidSize' : 'Page File Size (%@) must be less than Maximum Memory (%@).',
  '_configureRepositoryInfoDetailView.WriteQueuePageCountCache' : 'Page File Cache',
  '_configureRepositoryInfoDetailView.WriteQueuePageCountCache.Help' : 'Number of page files to keep in memory. The more files, the faster the performance.',
  '_configureRepositoryInfoDetailView.WriteQueuePageCountCache.Required' : 'Page File Cache is required.',
  '_configureRepositoryInfoDetailView.MaxKeywords' : 'Maximum Keywords',
  '_configureRepositoryInfoDetailView.MaxKeywords.Help' : 'Maximum number of keywords per log entry to save. The more keywords, the more memory that is used.',
  '_configureRepositoryInfoDetailView.MaxKeywords.Required' : 'Maximum Keywords is required.',
  '_configureRepositoryInfoDetailView.ReadDivider': 'Read Queue Attributes (NOTE: Read Queues have not been implemented)',
  '_configureRepositoryInfoDetailView.ReadQueueAddress' : 'Address',
  '_configureRepositoryInfoDetailView.ReadQueueUsername' : 'Login Username',
  '_configureRepositoryInfoDetailView.ReadQueuePassword' : 'Login Password',
  '_configureRepositoryInfoDetailView.ReadQueueDurable' : 'Durable?',
  '_configureRepositoryInfoDetailView.ReadQueueDurable.Yes' : 'Yes. <span class="help">Queued data saved to disk to prevent loss of data if server goes down.</span>',
  '_configureRepositoryInfoDetailView.ReadQueueDurable.No' : 'No. <span class="help">Queued data not saved to disk to improve throughput.</span>',
  '_configureRepositoryInfoDetailView.Status.Online': 'Online',
  '_configureRepositoryInfoDetailView.Status.Offline': 'Offline',
  '_configureRepositoryInfoDetailView.ConfirmDelete': 'Delete repository "%@"?',

  '_searchListView.Title': 'Search',
  '_searchListView.Search': 'Search',
  '_searchListView.Repository': 'In Repository',
  '_searchListView.TimeSpan': 'In The Past',
  '_searchListView.TimeSpan.5': '5 minutes',
  '_searchListView.TimeSpan.15': '15 minutes',
  '_searchListView.TimeSpan.30': '30 minutes',
  '_searchListView.TimeSpan.60': '60 minutes',
  '_searchListView.TimeSpan.1440': '24 hours',
  '_searchListView.TimeSpan.10080': '7 days',
  '_searchListView.TimeSpan.20160': '14 days',
  '_searchListView.TimeSpan.43200': '30 days',
  '_searchListView.Keywords': 'Keywords',
  '_searchListView.Timestamp': 'Timestamp',
  '_searchListView.Source': 'Source',
  '_searchListView.Host': 'Host',
  '_searchListView.Severity': 'Severity',
  '_searchListView.Message': 'Message',
  '_searchListView.Row': '#',
  '_searchListView.NoRowsFound': 'No matching entries found.',

  '_searchDetailView.Title': 'Repository Entry',
  '_searchDetailView.Timestamp': 'Timestamp',
  '_searchDetailView.Source': 'Source',
  '_searchDetailView.Host': 'Host',
  '_searchDetailView.Severity': 'Severity',
  '_searchDetailView.Message': 'Message',
  '_searchDetailView.Fields': 'Fields',
  '_searchDetailView.Keywords': 'Keywords',

  // ********************************************************
  // Data controllers
  // ********************************************************
  '_documentIDError': 'Expected document id "%@" but received "%@" from server.',

  '_sessionDataController.TokenNotFoundInResponseError': 'Token not found in authentication response',
  '_sessionDataController.VersionNotFoundInResponseError': 'Version number not found in authentication response',
  '_sessionDataController.BuildTimestampNotFoundInResponseError': 'Build timestamp not found in authentication response',

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

  'end': 'end'
});