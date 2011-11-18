/* 
    This file is part of Jongo.

    Jongo is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Jongo is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Jongo.  If not, see <http://www.gnu.org/licenses/>.

 */

Ext.require([
    'Ext.form.*',
    'Ext.layout.container.Absolute',
    'Ext.window.Window'
]);

Ext.onReady(function() {
    var form = Ext.create('Ext.form.Panel', {
        layout: 'absolute',
        url: 'save-form.php',
        defaultType: 'textfield',
        border: false,

        items: [{
            fieldLabel: 'Send To',
            fieldWidth: 60,
            msgTarget: 'side',
            allowBlank: false,
            x: 5,
            y: 5,
            name: 'to',
            anchor: '-5'  // anchor width by percentage
        }, {
            fieldLabel: 'Subject',
            fieldWidth: 60,
            x: 5,
            y: 35,
            name: 'subject',
            anchor: '-5'  // anchor width by percentage
        }, {
            x:5,
            y: 65,
            xtype: 'textarea',
            style: 'margin:0',
            hideLabel: true,
            name: 'msg',
            anchor: '-5 -5'  // anchor width and height
        }]
    });

    var win = Ext.create('Ext.window.Window', {
        title: 'Resize Me',
        width: 500,
        height: 300,
        minWidth: 300,
        minHeight: 200,
        layout: 'fit',
        plain:true,
        items: form,

        buttons: [{
            text: 'Send'
        },{
            text: 'Cancel'
        }]
    });

    win.show();
});