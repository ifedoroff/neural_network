var InputNode = draw2d.shape.basic.Circle.extend({
    NAME: 'InputNode',
    position: null,

    init: function(attr, setter, getter) {
        this._super($.extend({x:40,y:10, stroke:3, color:"#3d3d3d", bgColor:"#25d1fd"},attr), setter, getter);
        this.position = attr.position;
        this.createOutputPort();
    },

    createOutputPort: function createPort() {
        var newPort = null;
        var count = 0;
        newPort = new NoDuplicateOutputPort();
        newPort.setSemanticGroup('level1');
        count = this.outputPorts.getSize();

        newPort.setName("output" + count);

        this.addPort(newPort, new draw2d.layout.locator.RightLocator());
        // relayout the ports
        this.setDimension(this.width, this.height);

        return newPort;
    },

    onContextMenu:function(x,y){
        $.contextMenu({
            selector: '.InputNode',
            events:
                {
                    hide:function(){ $.contextMenu( 'destroy' ); }
                },
            callback: function(key, options)
            {
                switch(key){
                    case "delete":
                        this.fireEvent('delete');
                        break;

                    default:
                        break;
                }

            }.bind(this),
            x:x,
            y:y,
            items:
                {
                    "delete":  {name: $.i18n('delete')}
                }
        });
    },

    repaint: function(attrs) {
        this.portRelayoutRequired = true;
        this._super(attrs);
    }
});