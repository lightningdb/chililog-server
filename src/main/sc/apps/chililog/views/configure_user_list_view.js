// ==========================================================================
// Project:   Chililog
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

sc_require('views/image_view');
sc_require('views/radio_view');
sc_require('views/validators');
sc_require('views/label_mixin');

/**
 * Shows a list of users
 */
Chililog.ConfigureUserListView = SC.View.design({
  layout: { top: 0, left: 0, bottom: 0, right: 0 },
  childViews: 'title createButton refreshButton table'.w(),

  title: SC.LabelView.design({
    layout: { top: 5, left: 10, width: 200, height: 30 },
    tagName: 'h1',
    controlSize: SC.HUGE_CONTROL_SIZE,
    value: '_configureUserListView.Title',
    localize: YES
  }),

  createButton: SC.ButtonView.design({
    layout: { top: 40, left: 10, width: 130 },
    title: '_configureUserListView.Create',
    localize: YES,
    target: Chililog.configureUserListViewController,
    action: 'create'
  }),

  refreshButton: SC.ButtonView.design({
    layout: { top: 40, left: 150, width: 80 },
    title: '_refresh',
    localize: YES,
    isEnabledBinding: SC.Binding.from('Chililog.configureUserListViewController.isRefreshing').oneWay().not(),
    target: Chililog.configureUserListViewController,
    action: 'refresh'
  }),

  moreButton: SC.PopupButtonView.design({
    layout: { top: 40, left: 150, width: 130, height: 30 },
    classNames: ['button'],
    title: '_moreActions',
    localize: YES,
    menu: SC.MenuPane.design({
      layout: { width: 200 },
      items: [
        {
          title: '_deleteSelected'
        }
      ]
    })
  }),

  table: SC.TableView.design({
    layout: { top: 80, left: 10, right: 10, bottom: 10 },
    classNames: ['table'],
    contentBinding: 'Chililog.configureUserListViewController.arrangedObjects',
    selectionBinding: 'Chililog.configureUserListViewController.selection',
    useHeaders: YES,
    isEditable: NO,
    columns:[
      SC.TableColumn.create({
        key:   'username',
        title: '_configureUserDetailView.Username'.loc(),
        width: 150,
        isReorderable: NO   //Bug with reorder when switching with other configure options
      }),
      SC.TableColumn.create({
        key:   'emailAddress',
        title: '_configureUserDetailView.EmailAddress'.loc(),
        width: 250,
        isReorderable: NO
      }),
      SC.TableColumn.create({
        key:   'displayName',
        title: '_configureUserDetailView.DisplayName'.loc(),
        width: 250,
        isReorderable: NO,
        formatter:  function(v) {
          return SC.none(v) ? '' : v;
        }
      }),
      SC.TableColumn.create({
        key:   'currentStatus',
        title: '_configureUserDetailView.CurrentStatus'.loc(),
        width: 100,
        isReorderable: NO,

        /**
         * Special view for hi-lighting user status
         */
        exampleView: SC.LabelView.extend({
          layout: {left: 10, right: 10},
          isPoolable: YES,
          layerIsCacheable: YES,

          contentValueKeyBinding: '*column.key',
          contentValueKeyDidChange: function() {
            this.updatePropertyFromContent('value', '*', 'contentValueKey');
          }.observes('contentValueKey'),

          render: function(context, firstTime) {
            if (firstTime) {
              var value = this.get('value');
              if (!SC.none(value)) {
                var classArray = [];
                classArray.push('user-status-' + (this.get('value').toLowerCase()));
                context.addClass(classArray);
              }
            }
            sc_super();
          }
        })
      })
    ],
    target: Chililog.configureUserListViewController,
    action: 'edit'
  })

});

Chililog.configureUserListView = null;

