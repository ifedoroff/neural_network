var Neuron = draw2d.shape.basic.Circle.extend({

    NAME: 'Neuron',
    position: null,

    init: function(attr, setter, getter) {
        this._super($.extend({x:40,y:10, stroke:3, color:"#3d3d3d", bgColor:"yellow"},attr), setter, getter);
        this.position = attr.position;
        this.level = attr.level;
        // this.dummy = false;
        this.makeReal();
        this.createPort("output", new draw2d.layout.locator.RightLocator());
        this.createPort("input", new draw2d.layout.locator.LeftLocator());
    },

    createPort: function createPort(type, locator) {
        var newPort = null;
        var count = 0;

        switch (type) {
            case "input":
                newPort = new NoOutboundConnectionsInputPort();
                newPort.setSemanticGroup('level' + (this.level));
                console.log(newPort.getSemanticGroup());
                count = this.inputPorts.getSize();
                break;
            case "output":
                newPort = new NoDuplicateOutputPort();
                newPort.setSemanticGroup('level' + (this.level + 1));
                console.log(newPort.getSemanticGroup());
                count = this.outputPorts.getSize();
                break;
            default:
                return this._super(type, locator);
        }

        newPort.setName(type + count);

        this.addPort(newPort, locator);
        // relayout the ports
        this.setDimension(this.width, this.height);

        return newPort;
    },

    onContextMenu:function(x,y){
        $.contextMenu({
            selector: '.Neuron, .draw2d_shape_basic_Label',
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
                    case "dummy":
                        this.makeDummy();
                        break;
                    case "real":
                        this.makeReal();
                        break;
                    default:
                        break;
                }

            }.bind(this),
            x:x,
            y:y,
            items:
                {
                    "delete":  {name: "Delete"},
                    "dummy":  {name: "Dummy"},
                    "real":  {name: "Real"}
                }
        });
    },

    makeDummy: function() {
        this.dummy = true;
        this.setBackgroundColor('#c1b2bb');
        this.deleteLabel();
        this.repaint();
    },

    makeReal: function() {
        this.dummy = false;
        this.setBackgroundColor('yellow');
        this.createLabel();
        this.repaint();
    },

    createLabel: function(fun) {
        this.label = new draw2d.shape.basic.Label({
            text:fun||"lin(0.5)",
            fontColor:"#0d0d0d",
            stroke: 0
        });

        // add the new decoration to the connection with a position locator.
        //
        this.add(this.label, new draw2d.layout.locator.CenterLocator());

        this.label.installEditor(new draw2d.ui.LabelInplaceEditor());
        this.label.on('contextmenu', this.onContextMenu.bind(this));
    },

    deleteLabel: function() {
        this.remove(this.label);
        this.label = undefined;
    },

    repaint: function(attrs) {
        this.portRelayoutRequired = true;
        this._super(attrs);
        if(this.label) {
            this.label.repaint();
        }
    }

});