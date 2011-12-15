sessionUser = null

Ext.application({
    name: 'cidb',

    appFolder: 'mvc',
    
    controllers: [
        'CarsController'
    ],

    launch: drawViewPort
});


function drawViewPort(){
    var carsGrid = Ext.create('cidb.view.cars.List');
    
    Ext.create('Ext.container.Viewport', {
        layout: 'border',
        items: [
            {
                region: 'center',
                items:[
                    carsGrid
                ]
            },{
                region: 'west',
                xtype: 'toolbar',
                layout: 'vbox',
                items: [{
                    text: 'Users',
                    menu:{
                        items:[
                            { text: 'Show Users', action: 'showUsersWindow' },
                            { text: 'Add User', disabled: true, action: 'addUsersWindow' }
                        ]
                    }
                }, {
                    text: 'Statistics',
                    menu:{
                        items:[
                            {text: 'Car Sales by year', action:'show'},
                            {text: 'Car Sales by brand', action:'show'}
                        ]
                    }
                }, {
                    text: 'Pictures', action:'show'
                }, {
                    text: 'Comments', action:'show'
                }]
            }
        ]
    });
}