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
                        var neuronsLength = this.neurons.length;
                        for (let i = 0; i < neuronsLength; i++) {
                            this.deleteNeuron(this.neurons[0], true, false);
                        }
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
            message = "Входной слой должен иметь хотя бы один нейрон";
        } else {
            this.neurons.forEach(function (neuron) {
                if (neuron.getOutputPorts().get(0).getConnections().getSize() === 0) {
                    message = "Не все нейроны входного слоя имеют выходные веса";
                    return false;
                }
            });
        }
        return message;
    }
});