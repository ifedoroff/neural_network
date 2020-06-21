var Network = Class.extend({
    NAME: 'Network',

    init: function (attr, setter, getter) {
        this.tiers = new Tiers();
        canvas.add(this.tiers);
    },
    addNewTier: function() {
        return this.tiers.addNewTier();
    },

    addInputTier: function() {
        return this.tiers.addInputTier();
    },

    getData: function() {
        var message = this.validate();
        if(!message) {
            return this.tiers.getData();
        } else {
            alert(message);
            return undefined;
        }
    },

    read: function(data) {
        var inputTierSize = data.inputTierSize;
        var tiersConfig = data.tiers;
        var inputTier = this.tiers.addInputTier();
        for (let i = 0; i < inputTierSize; i++) {
            inputTier.addNeuron();
        }
        tiersConfig.forEach(function(tierConfig) {
            var tier = this.tiers.addNewTier();
            tierConfig.neurons.forEach(function(neuron) {
                tier.addNeuron(neuron.activationFn, neuron.isDummy)
            })
        }, this);
        tiersConfig.forEach(function(tierConfig, index) {
            var tier = this.tiers.getTier(index);
            var prevTier;
            if(index === 0) {
                prevTier = inputTier
            } else {
                prevTier = this.tiers.getTier(index - 1);
            }
            tierConfig.connections.forEach(function(connectionConfig) {
                canvas.add(new WeightedConnection({
                    source: prevTier.getNeuron(connectionConfig.source).getOutputPorts().get(0),
                    target: tier.getNeuron(connectionConfig.target).getInputPorts().get(0),
                    weight: connectionConfig.weight
                }));
            });
        }, this);
    },

    validate: function() {
        return this.tiers.validate();
    }
});