    var dom0 = document.querySelector('#form103 #field0');
    var field0 = new LiveValidation(dom0, {
      validMessage: "", onlyOnBlur: false, wait: 300}
                                   );
    var dom1 = document.querySelector('#form103 #field1');
    var field1 = new LiveValidation(dom1, {
      validMessage: "", onlyOnBlur: false, wait: 300}
                                   );
    field1.add(Validate.Length, {
      tooShortMessage:"Invalid length for field value", tooLongMessage: "Invalid length for field value",  minimum: 0, maximum: 35}
              );
    field1.add(Validate.Custom, {
      against: function(value) {
        return !value.match(/(telnet|ftp|https?):\/\/(?:[a-z0-9][a-z0-9-]{0,61}[a-z0-9]\.|[a-z0-9]\.)+[a-z]{2,63}/i);
      }
      , failureMessage: "Value must not contain any URL's"}
              );
    var dom2 = document.querySelector('#form103 #field2');
    var field2 = new LiveValidation(dom2, {
      validMessage: "", onlyOnBlur: false, wait: 300}
                                   );
    field2.add(Validate.Custom, {
      against: function(value) {
        return !value.match(/(telnet|ftp|https?):\/\/(?:[a-z0-9][a-z0-9-]{0,61}[a-z0-9]\.|[a-z0-9]\.)+[a-z]{2,63}/i);
      }
      , failureMessage: "Value must not contain any URL's"}
              );
    field2.add(Validate.Length, {
      tooShortMessage:"Invalid length for field value", tooLongMessage: "Invalid length for field value",  minimum: 0, maximum: 35}
              );
    var dom3 = document.querySelector('#form103 #field3');
    var field3 = new LiveValidation(dom3, {
      validMessage: "", onlyOnBlur: false, wait: 300}
                                   );
    var dom4 = document.querySelector('#form103 #field4');
    var field4 = new LiveValidation(dom4, {
      validMessage: "", onlyOnBlur: false, wait: 300}
                                   );
    field4.add(Validate.Custom, {
      against: function(value) {
        return !value.match(/(telnet|ftp|https?):\/\/(?:[a-z0-9][a-z0-9-]{0,61}[a-z0-9]\.|[a-z0-9]\.)+[a-z]{2,63}/i);
      }
      , failureMessage: "Value must not contain any URL's"}
              );
    field4.add(Validate.Length, {
      tooShortMessage:"Invalid length for field value", tooLongMessage: "Invalid length for field value",  minimum: 0, maximum: 35}
              );
    function resetSubmitButton(e){
      var submitButtons = e.target.form.getElementsByClassName('submit-button');
      for(var i=0;i<submitButtons.length;i++){
        submitButtons[i].disabled = false;
      }
    }
    function addChangeHandler(elements){
      for(var i=0; i<elements.length; i++){
        elements[i].addEventListener('change', resetSubmitButton);
      }
    }
    var form = document.getElementById('form103');
    addChangeHandler(form.getElementsByTagName('input'));
    addChangeHandler(form.getElementsByTagName('select'));
    addChangeHandler(form.getElementsByTagName('textarea'));
    var nodes = document.querySelectorAll('#form103 input[data-subscription]');
    if (nodes) {
      for (i = 0, len = nodes.length; i < len; i++) {
        var status = nodes[i].dataset ? nodes[i].dataset.subscription : nodes[i].getAttribute('data-subscription');
        if(status ==='true') {
          nodes[i].checked = true;
        }
      }
    };
    var nodes = document.querySelectorAll('#form103 select[data-value]');
    if (nodes) {
      for (var i = 0; i < nodes.length; i++) {
        var node = nodes[i];
        var selectedValue = node.dataset ? node.dataset.value : node.getAttribute('data-value');
        if (selectedValue) {
          for (var j = 0; j < node.options.length; j++) {
            if(node.options[j].value === selectedValue) {
              node.options[j].selected = 'selected';
              break;
            }
          }
        }
      }
    }