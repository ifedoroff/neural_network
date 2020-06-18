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
    }
});