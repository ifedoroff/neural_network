var Tiers = draw2d.shape.layout.HorizontalLayout.extend({
    NAME: 'Tiers',

    init(attr, setter, getter) {
        this._super($.extend({selectable: false, x: 50, y: 50}, attr), setter, getter);
        this.tiers = new draw2d.util.ArrayList();
        // this.inputTier = this.addInputTier();
        // this.inputTier.addInputNode();
        this.setGap(200);
        var _this = this;
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
        var tier = new Tier({level: this.getNextLevel()});
        tier.on('delete', this.deleteTier.bind(this, tier));
        this.tiers.add(tier);
        tier.on('neuronAdded', function() {
            this.calculateHeight();
        }, this);
        this.add(tier);
        return tier;
    },

    addInputTier: function() {
        var tier = new InputTier();
        tier.on('neuronAdded', function() {
            this.calculateHeight();
        }, this);
        this.add(tier);
        this.inputTier = tier;
        return tier;
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

    relayoutChildren: function() {

    },

    deleteTier: function(tier) {
        this.tiers.remove(tier);
        var cmd = new draw2d.command.CommandDelete(tier);
        canvas.getCommandStack().execute(cmd);
    },

    getNextLevel: function() {
        var levels = this.children.asArray().filter(function(it) {
            return it.figure.NAME === 'Tier'
        }).map(function(it){
            return it.figure.level;
        }).sort();
        return levels.length == 0 ? 1 : levels[levels.length - 1] + 1;

    }
});