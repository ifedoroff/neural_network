var ShowWeightSelectionPolicy = draw2d.policy.figure.SelectionPolicy.extend({
    onSelect: function(canvas, figure, isPrimarySelection) {
        if(figure instanceof WeightedConnection) {
            figure.label.setVisible(true);
        }
        this._super(canvas, figure, isPrimarySelection);
    },

    onUnselect: function(canvas, figure) {
        if(figure instanceof WeightedConnection) {
            figure.label.setVisible(false);
        }
        this._super(canvas, figure);
    }
});