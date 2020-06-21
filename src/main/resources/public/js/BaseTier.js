var BaseTier = draw2d.shape.basic.Rectangle.extend({

    NAME: "BaseTier",

    init:function(attr, setter, getter)
    {
        this._super( $.extend({stroke:1, bgColor:null, width:100, maxWidth: 100,height:110, resizeable: false},attr), setter, getter);
        this.level = attr.level;
        this.label = new draw2d.shape.basic.Label({text:this.getLabelText(), color:"#0d0d0d", fontColor:"#0d0d0d"});

        // add the new decoration to the connection with a position locator.
        //
        this.add(this.label, new draw2d.layout.locator.TopLocator(this));
        this.neurons = [];

        var port;
    },

    getLabelText: function() {
        return "";
    },

    addNeuron: function(activationFn, isDummy) {
        var position = this.getNextNeuronPosition();
        var neuron = this.createNeuron(position,activationFn, isDummy);
        neuron.attr(this.calculateNeuronPosition(position, neuron));
        this.neurons.push(neuron);
        neuron.on('delete', this.deleteNeuron.bind(this, neuron));
        this.add(neuron, new draw2d.layout.locator.Locator());
        this.repaint();
        this.fireEvent('neuronAdded');
    },

    createNeuron: function(position, activationFn, isDummy) {
        return null;
    },

    calculateNeuronPosition: function(index, neuron) {
        if(index == 0) {
            var parentBox = this.getOuterBoundingBox();
            return {
                x: parentBox.w/2 - neuron.width / 2,
                y: 30
            }
        } else {
            var previousNeuron = this.neurons[index - 1];
            return {
                y: previousNeuron.y + previousNeuron.height + 30,
                x: previousNeuron.x
            }
        }
    },

    repaint: function(attr) {
        if(!!this.neurons && this.neurons.length > 0) {
            this.height = this.neurons.length * (this.neurons[0].height + 30) + 30;
        }
        this._super(attr);
    },

    getNextNeuronPosition: function() {
        return this.neurons.length;
    },

    getNeuron: function(position) {
        return this.neurons[position];
    },

    deleteNeuron: function(neuron, force) {
        if(this.neurons.length > 1 || force) {
            neuron.outputPorts.each(function(i, port) {
                port.connections.each(function(j, conn) {
                    conn.disconnect();
                    canvas.remove(conn);
                });
            });
            neuron.inputPorts.each(function(i, port) {
                port.connections.each(function(j, conn) {
                    conn.disconnect();
                    canvas.remove(conn);
                });
            });
            this.remove(neuron);
            this.neurons.splice(this.neurons.indexOf(neuron), 1);
        }
        this.reorderNeurons();
        this.repaint();
    },

    reorderNeurons: function() {
        this.neurons.forEach(function(neuron, index) {
            neuron.position = index;
            neuron.attr(this.calculateNeuronPosition(index, neuron));
        }, this);
    },

    applyAlpha: function(){
    }
});

