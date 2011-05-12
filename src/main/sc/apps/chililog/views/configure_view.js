// ==========================================================================
// Project:   Chililog
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

sc_require('views/image_view');
sc_require('views/radio_view');
sc_require('views/validators');
sc_require('views/label_mixin');

/**
 * Configure view
 */
Chililog.ConfigureView = SC.View.design({
  layout: { top: 10, left: 10, bottom: 10, right: 10 },
  childViews: 'title left right'.w(),

  title: SC.LabelView.design({
    layout: { top: 0, left: 0, width: 200, height: 30 },
    tagName: 'h1',
    controlSize: SC.HUGE_CONTROL_SIZE,
    value: '_configureView.Title',
    localize: YES
  }),

  left: SC.ScrollView.design({
    layout: { top: 35, left: 0, bottom: 0, width: 200 },
    classNames: ['list-menu'],

    contentView: SC.ListView.design({
      layout: { top: 0, bottom: 0, left: 0, right: 0 },
      rowHeight: 40,
      isEditable: NO,
      actOnSelect: YES,
      content: [
        {
          id: 'Repositories',
          label: '_configureView.Repositories'.loc(),
          icon: sc_static('images/repositories.png')
        },
        {
          id: 'Users',
          label: '_configureView.Users'.loc(),
          icon: sc_static('images/users.png')
        }
      ],
      hasContentIcon: YES,
      contentValueKey: 'label',
      contentIconKey: 'icon',
      target: Chililog.configureViewController,
      action: 'onSelect'
    })
  }),

  right: SC.ContainerView.design({
    layout: { top: 35, bottom: 0, left: 208, right: 0 },
    classNames: ['box']
  })

});

/**
 * Instance configure view
 */
Chililog.configureView = Chililog.ConfigureView.create();

/**
 * Scene views
 * We cannot reuse just the 1 scene view because we get funny overlap issues after navigating away and then back
 */
Chililog.ConfigureRepositoryInfoSceneView = SC.SceneView.design({
  layout: { top: 0, bottom: 0, left: 0, right: 0 },
  scenes: ['Chililog.configureRepositoryInfoListView', 'Chililog.configureRepositoryInfoDetailView']
});
Chililog.configureRepositoryInfoSceneView = Chililog.ConfigureRepositoryInfoSceneView.create();

Chililog.ConfigureUserSceneView = SC.SceneView.design({
  layout: { top: 0, bottom: 0, left: 0, right: 0 },
  scenes: ['Chililog.configureUserListView', 'Chililog.configureUserDetailView']
});
Chililog.configureUserSceneView = Chililog.ConfigureUserSceneView.create();
