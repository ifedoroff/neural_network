var InputTier = BaseTier.extend({
    NAME: "InputTier",

    init:function(attr, setter, getter) {
        this._super($.extend({level: 0}, attr), setter, getter);
    },

    getLabelText: function() {
        return 'Input Tier';
    },

    createNeuron: function(position) {
        return new InputNode({position: position, level: this.level})
    },

    onContextMenu:function(x,y){
        $.contextMenu({
            selector: '.InputTier, .draw2d_shape_basic_Rectangle',
            events:
                {
                    hide:function(){ $.contextMenu( 'destroy' ); }
                },
            callback: function(key, options)
            {
                switch(key){
                    case "addNeuron":
                        this.addNeuron();
                        break;
                    case "delete":
                        this.neurons.forEach(function(neuron) {
                            this.deleteNeuron(neuron, true);
                        }, this);
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
                    "addNeuron":    {name: "Add Input Node"},
                    "delete":  {name: "Delete"}
                }
        });
    },

    validate: function() {
        var message;
        if( this.neurons.length === 0) {
            message = this.label.getText() + " should have at least one neuron";
        } else {
            this.neurons.forEach(function (neuron) {
                if (neuron.getOutputPorts().get(0).getConnections().getSize() === 0) {
                    message = "Not all neurons of the Input Tier have outbound connections";
                    return false;
                }
            });
        }
        return message;
    }
});