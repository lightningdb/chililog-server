// ==========================================================================
// Project:   Chililog
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

sc_require('views/image_view');
sc_require('views/radio_view');
sc_require('views/validators');
sc_require('views/label_mixin');

/**
 * Repositories Listing
 */
Chililog.ConfigureRepositoryInfoListView = SC.View.design({
  layout: { top: 0, left: 0, bottom: 0, right: 0 },
  childViews: 'title createButton refreshButton toggleStartStopButton table'.w(),

  title: SC.LabelView.design({
    layout: { top: 5, left: 10, width: 200, height: 30 },
    tagName: 'h1',
    controlSize: SC.HUGE_CONTROL_SIZE,
    value: '_configureRepositoryInfoListView.Title',
    localize: YES
  }),

  createButton: SC.ButtonView.design({
    layout: { top: 40, left: 10, width: 170 },
    title: '_configureRepositoryInfoListView.Create',
    localize: YES,
    target: Chililog.configureRepositoryInfoListViewController,
    action: 'create'
  }),

  refreshButton: SC.ButtonView.design({
    layout: { top: 40, left: 190, width: 80 },
    title: '_refresh',
    localize: YES,
    isEnabledBinding: SC.Binding.from('Chililog.configureRepositoryInfoListViewController.isRefreshing').oneWay().not(),
    target: Chililog.configureRepositoryInfoListViewController,
    action: 'refresh'
  }),

  toggleStartStopButton: SC.ButtonView.design({
    layout: { top: 40, left: 280, width: 80 },
    titleBinding: 'Chililog.configureRepositoryInfoListViewController.toggleStartStopButtonTitle',
    isVisibleBinding: SC.Binding.from('Chililog.configureRepositoryInfoListViewController.selectedRecord').oneWay().bool(),
    target: Chililog.configureRepositoryInfoListViewController,
    action: 'toggleStartStop'
  }),

  table: SC.TableView.design({
    layout: { top: 80, left: 10, right: 10, bottom: 10 },
    classNames: ['table'],
    contentBinding: 'Chililog.configureRepositoryInfoListViewController.arrangedObjects',
    selectionBinding: 'Chililog.configureRepositoryInfoListViewController.selection',
    useHeaders: YES,
    isEditable: NO,
    columns:[
      SC.TableColumn.create({
        key:   'name',
        title: '_configureRepositoryInfoDetailView.Name'.loc(),
        width: 150,
        isReorderable: NO
      }),
      SC.TableColumn.create({
        key:   'displayName',
        title: '_configureRepositoryInfoDetailView.DisplayName'.loc(),
        width: 250,
        isReorderable: NO,
        formatter:  function(v) {
          return SC.none(v) ? '' : v;
        }
      }),
      SC.TableColumn.create({
        key:   'currentStatus',
        title: '_configureRepositoryInfoDetailView.CurrentStatus'.loc(),
        width: 120,
        isReorderable: NO,
        formatter:  function(v) {
          return v === 'ONLINE' ? '_configureRepositoryInfoDetailView.Status.Online'.loc() : '_configureRepositoryInfoDetailView.Status.Offline'.loc();
        },
        
        /**
         * Special view for hi-lighting Offline repositories on RED
         * Note that we bind to the status code and not text so that we can standardise on the user of the
         * status code in the CSS class name.
         */
        exampleView: SC.TableCellContentView.extend({
          render: function(context, firstTime) {
            if (!firstTime) {
              context.removeClass('repository-status-online');
              context.removeClass('repository-status-offline');
            }
            var value = this.get('value');
            if (!SC.none(value)) {
              context.addClass('repository-status-' + (value.toLowerCase()));
            }
            sc_super();
          }
        })
      }),
      SC.TableColumn.create({
        key:   'description',
        title: '_configureRepositoryInfoDetailView.Description'.loc(),
        width: 300,
        isReorderable: NO,
        formatter:  function(v) {
          return SC.none(v) ? '' : v;
        }
      })
    ],
    target: Chililog.configureRepositoryInfoListViewController,
    action: 'edit',

    /**
     * Reset when visible to make sure that screen is displayed correctly when show/not showing in container views
     */
    doReset: function() {
      var isVisibleInWindow = this.get('isVisibleInWindow');
      if (isVisibleInWindow) {
        var x = this.getPath('_dataView.contentView');
        x._reset(0);
      }
    }.observes('isVisibleInWindow')    
  })

});

Chililog.configureRepositoryInfoListView = Chililog.ConfigureRepositoryInfoListView.create();


