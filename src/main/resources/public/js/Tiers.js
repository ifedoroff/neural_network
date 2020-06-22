var Tiers = draw2d.shape.layout.HorizontalLayout.extend({
    NAME: 'Tiers',

    init(attr, setter, getter) {
        this._super($.extend({selectable: false, x: 50, y: 50}, attr), setter, getter);
        this.tiers = new draw2d.util.ArrayList();
        // this.inputTier = this.addInputTier();
        // this.inputTier.addInputNode();
        this.setGap(400);
        var _this = this;
        this.renderBlocked = false;
        this.locator.relocate = function relocate(index, target) {
            var stroke = _this.getStroke();
            var yPos = stroke + _this.padding.top + (_this.getHeight() - target.getHeight()) / 2;
            var xPos = stroke + _this.padding.left; // respect the border and padding of the parent
            for (var i = 0; i < index; i++) {
                var child = _this.children.get(i).figure;
                if (child.isVisible()) {
                    xPos += child.getWidth() + _this.gap;
                }
            }
            target.setPosition(xPos, yPos);
        };
    },



    addNewTier: function() {
        var tier = new Tier({level: this.getNextLevel(), isOutputTier: true});
        tier.on('delete', this.deleteTier.bind(this, tier));
        if(this.tiers.last() !== undefined) {
            this.tiers.last().setOutputTier(false);
        }
        this.tiers.add(tier);
        tier.on('neuronAdded', function() {
            this.calculateHeight();
        }, this);
        this.add(tier);
        return tier;
    },

    getTier: function(index) {
        return this.tiers.get(index);
    },

    addInputTier: function() {
        if(this.inputTier === undefined) {
            var tier = new InputTier();
            tier.on('delete', this.deleteInputTier.bind(this, tier));
            tier.on('neuronAdded', function () {
                this.calculateHeight();
            }, this);
            this.add(tier, null, 0);
            this.inputTier = tier;
            return tier;
        }
    },

    calculateHeight: function() {
        var maxHeight = 0;
        this.children.each(function(i, tier){
            maxHeight = Math.max(tier.figure.height, maxHeight);
        });
        this.setHeight(maxHeight);
        this.tiers.each(function(i, tier) {
            tier.repaint();
        });
        this.inputTier.repaint();
    },

    deleteTier: function(tier) {
        this.tiers.remove(tier);
        var tiers = this.tiers;
        this.remove(tier);
        this.tiers.each(function(i, tier) {
            tier.setOutputTier(i + 1 === tiers.getSize());
            tier.setLevel(i + 1);
        });
        this.repaint();
        var cmd = new draw2d.command.CommandDelete(tier);
        canvas.getCommandStack().execute(cmd);
    },

    deleteInputTier: function(tier) {
        this.tiers.remove(tier);
        this.inputTier = undefined;
        this.remove(tier);
        this.repaint();
        // var cmd = new draw2d.command.CommandDelete(tier);
        // canvas.getCommandStack().execute(cmd);
    },

    getNextLevel: function() {
        return this.tiers.isEmpty() ? 1 : this.tiers.getSize() + 1;
    },

    validate: function() {
        var message;
        if(this.inputTier === undefined) {
            message = "Добавьте входной слой";
        } else if(this.tiers.getSize() < 2) {
            message = "Необходимо добавить хотя бы один скрытый и один выходной слой";
        } else {
            message = this.inputTier.validate();
            if(message === undefined) {
                this.tiers.each(function (i, tier) {
                    message = tier.validate();
                    if (message !== undefined) {
                        return false;
                    }
                });
            }
        }
        return message;
    },

    getData: function() {
        var tiers = [];
        var me = this;
        this.tiers.each(function(index, tier) {
            var connections = [];
            var neurons = tier.neurons.map(function(neuron) {
                return {
                    isDummy: neuron.isDummy(),
                    activationFn: neuron.getActivationFn()
                }
            });
            tier.neurons.forEach(function(neuron) {
                neuron.getInputPorts().each(function(i, port) {
                    port.getConnections().each(function(j, connection) {
                        connections.push({
                            source: connection.getSource().getParent().position,
                            target: neuron.position,
                            weight: Number(connection.getWeight())
                        });
                    });
                });
            });
            tiers.push({
                neurons: neurons,
                connections: connections
            })
        });
        return {
            inputTierSize: this.inputTier.neurons.length,
            tiers: tiers,
        };
    }
});