// ==========================================================================
// Project:   Chililog
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

/**
 * Search details view
 */
Chililog.SearchDetailView = SC.PanelPane.design({
  layout: { width:800, height:500, centerX:0, centerY:-50 },

  contentView: SC.View.extend({
    childViews: 'title data previousButton nextButton doneButton'.w(),

    title: SC.LabelView.design({
      layout: { top: 10, left: 10, right: 10, height: 30 },
      tagName: 'h1',
      controlSize: SC.HUGE_CONTROL_SIZE,
      value: '_searchDetailView.Title',
      localize: YES
    }),

    data: SC.View.extend({
      layout: { top: 50, left: 10, right: 10, bottom: 50 },
      classNames: ['border'],
      childViews: 'timestamp source host severity message fields keywords'.w(),

      timestamp: SC.View.design({
        layout: {top: 10, left: 10, width: 200, height: 50 },
        classNames: ['up-down-data-item'],
        childViews: 'label field'.w(),

        label: SC.LabelView.design({
          layout: { top: 0, left: 0, width: 195, height: 20 },
          value: '_searchDetailView.Timestamp'.loc()
        }),

        field: SC.TextFieldView.design({
          layout: { top: 20, left: 0, width: 195, height: 28 },
          valueBinding: 'Chililog.searchDetailViewController.timestampText'
        })
      }),

      source: SC.View.design({
        layout: {top: 10, left: 220, width: 230, height: 50 },
        classNames: ['up-down-data-item'],
        childViews: 'label field'.w(),

        label: SC.LabelView.design({
          layout: { top: 0, left: 0, width: 100, height: 20 },
          value: '_searchDetailView.Source'.loc()
        }),

        field: SC.TextFieldView.design({
          layout: { top: 20, left: 0, width: 225, height: 28 },
          maxlength: 200,
          valueBinding: 'Chililog.searchDetailViewController.source'
        })
      }),

      host: SC.View.design({
        layout: {top: 10, left: 460, width: 150, height: 50 },
        classNames: ['up-down-data-item'],
        childViews: 'label field'.w(),

        label: SC.LabelView.design({
          layout: { top: 0, left: 0, width: 145, height: 20 },
          value: '_searchDetailView.Host'.loc()
        }),

        field: SC.TextFieldView.design({
          layout: { top: 20, left: 0, width: 145, height: 28 },
          maxlength: 200,
          valueBinding: 'Chililog.searchDetailViewController.host'
        })
      }),

      severity: SC.View.design({
        layout: {top: 10, left: 620, width: 150, height: 50 },
        classNames: ['up-down-data-item'],
        childViews: 'label field'.w(),

        label: SC.LabelView.design({
          layout: { top: 0, left: 0, width: 145, height: 20 },
          value: '_searchDetailView.Severity'.loc()
        }),

        field: SC.TextFieldView.design({
          layout: { top: 20, left: 0, width: 145, height: 28 },
          valueBinding: 'Chililog.searchDetailViewController.severityText'
        })
      }),

      message: SC.View.design({
        layout: {top: 70, left: 10, right: 10, height: 150 },
        classNames: ['up-down-data-item'],
        childViews: 'label field'.w(),

        label: SC.LabelView.design({
          layout: { top: 0, left: 0, width: 100, height: 20 },
          value: '_searchDetailView.Message'.loc()
        }),

        field: SC.TextFieldView.design({
          layout: { top: 20, left: 0, right: 0, height: 128 },
          isTextArea: YES,
          valueBinding: 'Chililog.searchDetailViewController.message'
        })
      }),
      
      fields: SC.View.design({
        layout: {top: 230, left: 10, right: 10, height: 100 },
        classNames: ['up-down-data-item'],
        childViews: 'label field'.w(),

        label: SC.LabelView.design({
          layout: { top: 0, left: 0, width: 100, height: 20 },
          value: '_searchDetailView.Fields'.loc()
        }),

        field: SC.TextFieldView.design({
          layout: { top: 20, left: 0, right: 0, height: 78 },
          isTextArea: YES,
          valueBinding: 'Chililog.searchDetailViewController.fieldsText'
        })
      }),

      keywords: SC.View.design({
        layout: {top: 340, left: 10, right: 10, height: 50 },
        classNames: ['up-down-data-item'],
        childViews: 'label field'.w(),

        label: SC.LabelView.design({
          layout: { top: 0, left: 0, width: 100, height: 20 },
          value: '_searchDetailView.Keywords'.loc()
        }),

        field: SC.TextFieldView.design({
          layout: { top: 20, left: 0, right: 0, height: 28 },
          maxlength: 200,
          valueBinding: 'Chililog.searchDetailViewController.keywordsText'
        })
      })

    }),

    previousButton: SC.ButtonView.design({
      layout: { top: 460, left: 10, width: 40 },
      title: '_previous',
      localize: YES,
      controlSize: SC.HUGE_CONTROL_SIZE,
      target: Chililog.searchDetailViewController,
      action: 'previous'
    }),

    nextButton: SC.ButtonView.design({
      layout: { top: 460, left: 60, width: 40 },
      title: '_next',
      localize: YES,
      controlSize: SC.HUGE_CONTROL_SIZE,
      target: Chililog.searchDetailViewController,
      action: 'next'
    }),

    doneButton: SC.ButtonView.design({
      layout: { top: 460, right: 10, width: 80 },
      title: '_done',
      localize: YES,
      controlSize: SC.HUGE_CONTROL_SIZE,
      isDefault: YES,
      target: Chililog.searchDetailViewController,
      action: 'done'
    })
  })
});

/**
 * Instance the search details view
 */
Chililog.searchDetailView = Chililog.SearchDetailView.create();
