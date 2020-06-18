var Tier = BaseTier.extend({

    NAME: "Tier",

    init:function(attr, setter, getter) {
        this._super(attr, setter, getter);
    },

    getLabelText: function() {
        return 'Tier ' + this.level
    },

    createNeuron: function(position) {
        return new Neuron({position: position, level: this.level})
    },

    onContextMenu:function(x,y){
        $.contextMenu({
            selector: '.Tier, .draw2d_shape_basic_Rectangle',
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
                    case "addNeuron":
                        this.addNeuron();
                        break;
                    default:
                        break;
                }

            }.bind(this),
            x:x,
            y:y,
            items:
                {
                    "addNeuron":    {name: "Add Neuron"},
                    "delete":  {name: "Delete"}
                }
        });
    }
});

