var ShowWeightSelectionPolicy = draw2d.policy.figure.SelectionPolicy.extend({
    onSelect: function(canvas, figure, isPrimarySelection) {
        if(figure instanceof WeightedConnection) {
            figure.label.setVisible(true);
        }
    },

    onUnselect: function(canvas, figure) {
        if(figure instanceof WeightedConnection) {
            figure.label.setVisible(false);
        }
    }
});