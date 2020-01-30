(function ($) {
  'use strict';

  var caretClass   = 'textarea-helper-caret'
    , dataKey      = 'textarea-helper'

    // Styles that could influence size of the mirrored element.
    , mirrorStyles = [ 
                       // Box Styles.
                       'box-sizing', 'height', 'width', 'padding-bottom'
                     , 'padding-left', 'padding-right', 'padding-top'
  
                       // Font stuff.
                     , 'font-family', 'font-size', 'font-style' 
                     , 'font-variant', 'font-weight'
  
                       // Spacing etc.
                     , 'word-spacing', 'letter-spacing', 'line-height'
                     , 'text-decoration', 'text-indent', 'text-transform' 
                     
                      // The direction.
                     , 'direction'
                     ];

  var TextareaHelper = function (elem) {
    if (elem.nodeName.toLowerCase() !== 'textarea') return;
    this.$text = $(elem);
    this.$mirror = $('<div/>').css({ 'position'    : 'absolute'
                                   , 'overflow'    : 'auto'
                                   , 'white-space' : 'pre-wrap'
                                   , 'word-wrap'   : 'break-word'
                                   , 'top'         : 0
                                   , 'left'        : -9999
                                   }).insertAfter(this.$text);
  };

  (function () {
    this.update = function () {

      // Copy styles.
      var styles = {};
      for (var i = 0, style; style = mirrorStyles[i]; i++) {
        styles[style] = this.$text.css(style);
      }
      this.$mirror.css(styles).empty();
      
      // Update content and insert caret.
      var caretPos = this.getOriginalCaretPos()
        , str      = this.$text.val()
        , pre      = document.createTextNode(str.substring(0, caretPos))
        , post     = document.createTextNode(str.substring(caretPos))
        , $car     = $('<span/>').addClass(caretClass).css('position', 'absolute').html('&nbsp;');
      this.$mirror.append(pre, $car, post)
                  .scrollTop(this.$text.scrollTop());
    };

    this.destroy = function () {
      this.$mirror.remove();
      this.$text.removeData(dataKey);
      return null;
    };

    this.caretPos = function () {
      this.update();
      var $caret = this.$mirror.find('.' + caretClass)
        , pos    = $caret.position();
      if (this.$text.css('direction') === 'rtl') {
        pos.right = this.$mirror.innerWidth() - pos.left - $caret.width();
        pos.left = 'auto';
      }

      return pos;
    };

    this.height = function () {
      this.update();
      this.$mirror.css('height', '');
      return this.$mirror.height();
    };

    // XBrowser caret position
    // Adapted from http://stackoverflow.com/questions/263743/how-to-get-caret-position-in-textarea
    this.getOriginalCaretPos = function () {
      var text = this.$text[0];
      if (text.selectionStart) {
        return text.selectionStart;
      } else if (document.selection) {
        text.focus();
        var r = document.selection.createRange();
        if (r == null) {
          return 0;
        }
        var re = text.createTextRange()
          , rc = re.duplicate();
        re.moveToBookmark(r.getBookmark());
        rc.setEndPoint('EndToStart', re);
        return rc.text.length;
      } 
      return 0;
    };

  }).call(TextareaHelper.prototype);
  
  $.fn.textareaHelper = function (method) {
    this.each(function () {
      var $this    = $(this)
        , instance = $this.data(dataKey);
      if (!instance) {
        instance = new TextareaHelper(this);
        $this.data(dataKey, instance);
      }
    });
    if (method) {
      var instance = this.first().data(dataKey);
      return instance[method]();
    } else {
      return this;
    }
  };

})(jQuery);
