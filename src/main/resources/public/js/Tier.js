var Tier = BaseTier.extend({

    NAME: "Tier",

    init:function(attr, setter, getter) {
        this._super(attr, setter, getter);
        this.isOutputTier = attr.isOutputTier || false;
    },

    getLabelText: function() {
        return $.i18n('tier') + ' ' + this.level;
    },

    createNeuron: function(position, activationFn, isDummy) {
        return new Neuron({position: position, level: this.level, activationFn: activationFn, isDummy: isDummy})
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
                        var neuronsLength = this.neurons.length;
                        for (let i = 0; i < neuronsLength; i++) {
                            this.deleteNeuron(this.neurons[0], true, false);
                        }
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
                    "addNeuron":    {name: $.i18n('addNeuron')},
                    "delete":  {name: $.i18n('delete')}
                }
        });
    },

    validate: function() {
        var message;
        if( this.neurons.length === 0) {
            message = this.label.getText() + " должен иметь хотя бы один нейрон";
        } else {
            this.neurons.forEach(function (neuron) {
                if ((!this.isOutputTier && neuron.getOutputPorts().get(0).getConnections().getSize() === 0) || neuron.getInputPorts().get(0).getConnections().getSize() === 0) {
                    message = "Некоторые нейроны слоя " + this.label.getText() + " не имеют входных и/или выходных весов";
                    return false;
                }
            }, this);
        }
        return message;
    },

    setOutputTier: function(isOutputTier) {
        if(this.isOutputTier !== isOutputTier) {
            this.isOutputTier = isOutputTier;
        }
    },

    setLevel: function(level) {
        this.level = level;
        this.label.setText(this.getLabelText());
        this.neurons.forEach(function(neuron) {
            neuron.setLevel(level);
        })
    }
});

