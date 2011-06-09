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
  childViews: 'left right'.w(),

  left: SC.ScrollView.design({
    layout: { top: 0, left: 0, bottom: 0, width: 200 },
    classNames: ['list-menu'],

    contentView: SC.ListView.design({
      layout: { top: 0, bottom: 0, left: 0, right: 0 },
      rowHeight: 40,
      isEditable: NO,
      actOnSelect: YES,
      contentBinding: 'Chililog.configureViewController.menuItems',
      hasContentIcon: YES,
      contentValueKey: 'label',
      contentIconKey: 'icon',
      target: Chililog.configureViewController,
      action: 'onSelect'
    })
  }),

  right: SC.ContainerView.design({
    layout: { top: 0, bottom: 0, left: 208, right: 0 },
    classNames: ['box']
  })

});

/**
 * Instance configure view
 */
Chililog.configureView = Chililog.ConfigureView.create();
