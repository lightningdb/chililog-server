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
  '_delete': 'Delete',
  '_deleteSelected': 'Delete Selected',
  '_back': '\< Back',
  '_moreActions': 'More Actions',
  '_saveSuccess': 'Changes successfully saved',
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
  '_loginPane.Password': 'Password',
  '_loginPane.RememberMe': 'Remember Me',
  '_loginPane.Login': 'Login',

  '_myAccountView.Title': 'My Account',
  '_myAccountView.MyProfile': 'My Profile',
  '_myAccountView.Username': 'Username',
  '_myAccountView.EmailAddress': 'Email Address',
  '_myAccountView.DisplayName': 'Display Name',
  '_myAccountView.DisplayNameHelp': '(Optional name or nickname to display instead of the username)',
  '_myAccountView.ChangePassword': 'Change My Password',
  '_myAccountView.OldPassword': 'Old Password',
  '_myAccountView.NewPassword': 'New Password',
  '_myAccountView.ConfirmPassword': 'Confirm New Password',
  '_myAccountView.ChangePasswordSuccess': 'Password changed',

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
  '_configureUserDetailView.EmailAddress': 'Email Address',
  '_configureUserDetailView.DisplayName': 'Display Name',
  '_configureUserDetailView.DisplayNameHelp': '(Optional name or nickname to display instead of the username)',
  '_configureUserDetailView.CurrentStatus': 'Status',
  '_configureUserDetailView.Password': 'Password',
  '_configureUserDetailView.ConfirmPassword': 'Confirm Password',
  '_configureUserDetailView.ConfirmDelete': 'Delete user "%@"?',
  '_configureRepositoryInfoListView.Title': 'Repositories',
  '_configureRepositoryInfoListView.Create': 'Create a New Repository',
  '_configureRepositoryInfoDetailView.EditTitle': 'Repository: %@',
  '_configureRepositoryInfoDetailView.CreateTitle': 'New Repository',
  '_configureRepositoryInfoDetailView.Title': 'Repository',
  '_configureRepositoryInfoDetailView.Name': 'Name',
  '_configureRepositoryInfoDetailView.NameHelp': 'Only lower case letters (a-z), digits (0-9) and underscore (_) characters allowed.',
  '_configureRepositoryInfoDetailView.DisplayName': 'Display Name',
  '_configureRepositoryInfoDetailView.Description': 'Description',
  '_configureRepositoryInfoDetailView.Status': 'Status',
  '_configureRepositoryInfoDetailView.WriteDivider': 'Write Queue Attributes',
  '_configureRepositoryInfoDetailView.WriteQueueAddress' : 'Address',
  '_configureRepositoryInfoDetailView.WriteQueueUsername' : 'Login Username',
  '_configureRepositoryInfoDetailView.WriteQueuePassword' : 'Login Password',
  '_configureRepositoryInfoDetailView.WriteQueueDurable' : 'Durable?',
  '_configureRepositoryInfoDetailView.WriteQueueWorkerCount' : 'Worker Count',
  '_configureRepositoryInfoDetailView.WriteQueueWorkerCountHelp' : 'Number of worker threads that will consume log entries from this queue and write them to the database.',
  '_configureRepositoryInfoDetailView.WriteQueueMaxMemory' : 'Maximum Memory (bytes)',
  '_configureRepositoryInfoDetailView.WriteQueueMaxMemoryHelp' : 'Maximum memory that will be used to store queued items.',
  '_configureRepositoryInfoDetailView.WriteQueueMaxMemoryPolicy' : 'Maximum Memory Policy',
  '_configureRepositoryInfoDetailView.WriteQueuePageSize' : 'Page File Size',
  '_configureRepositoryInfoDetailView.WriteQueuePageSizeHelp' : 'The size of each page file. Only applicable for Paging Mode.',
  '_configureRepositoryInfoDetailView.WriteQueuePageCountCache' : 'Page File Cache',
  '_configureRepositoryInfoDetailView.WriteQueuePageCountCacheHelp' : 'Number of page files to keep in memory. The more files, the faster the performance.',
  '_configureRepositoryInfoDetailView.MaxKeywords' : 'Maximum Keywords',
  '_configureRepositoryInfoDetailView.MaxKeywordsHelp' : 'Maximum number of keywords per log entry to save. The more keywords, the more memory that is used.',
  '_configureRepositoryInfoDetailView.ReadDivider': 'Read Queue Attributes (NOTE: Read Queues have not been implemented)',
  '_configureRepositoryInfoDetailView.ReadQueueAddress' : 'Address',
  '_configureRepositoryInfoDetailView.ReadQueueUsername' : 'Login Username',
  '_configureRepositoryInfoDetailView.ReadQueuePassword' : 'Login Password',
  '_configureRepositoryInfoDetailView.ReadQueueDurable' : 'Durable?',


  // ********************************************************
  // Data controllers
  // ********************************************************

  '_sessionDataController.UsernameRequiredError': 'Username is required',
  '_sessionDataController.PasswordRequiredError': 'Password is required',
  '_sessionDataController.EmailAddressRequiredError': 'Email Address is required',
  '_sessionDataController.DisplayNameRequiredError': 'Display Name is required',
  '_sessionDataController.TokenNotFoundInResponseError': 'Token not found in authentication response',
  '_sessionDataController.VersionNotFoundInResponseError': 'Version number not found in authentication response',
  '_sessionDataController.BuildTimestampNotFoundInResponseError': 'Build timestamp not found in authentication response',
  '_sessionDataController.OldNewConfirmPasswordRequiredError': 'Old, new and confirm passwords are required.',
  '_sessionDataController.ConfirmPasswordError': 'New Password and Confirm New Passwords are not the same. Please re-enter you new password again.',

  '_userDataController.UsernameRequiredError': 'Username is required',
  '_userDataController.PasswordRequiredError': 'Password is required',
  '_userDataController.ConfirmPasswordRequiredError': 'New Password and Confirm New Passwords are not the same. Please re-enter you new password again.',
  '_userDataController.EmailAddressRequiredError': 'Email Address is required',

  '_repositoryInfoDataController.NameRequiredError': 'Name is requried',

  
  'end': 'end'
});