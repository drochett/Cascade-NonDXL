function requestStarted() { 
         if(this.fullStandby==null) { 
                 this.fullStandby = new dojox.widget.Standby({ 
                         target: document.forms[0], // it was dojo.body() which creates a problem in some dojo versions. 
                         image:  'ajax-loader.gif',
                         imageText: 'Stand By'
                 }); 
                 document.body.appendChild(fullStandby.domNode); 
                  fullStandby.startup(); 
         } 
         fullStandby.show(); 
 }  
 
 function requestCompleted() { 
         if(this.fullStandby!=null) fullStandby.hide(); 
 }