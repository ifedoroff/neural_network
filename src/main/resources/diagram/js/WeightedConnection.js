var WeightedConnection = draw2d.Connection.extend({

    NAME: 'WeightedConnection',

    init:function(attr) {
        this._super(attr);

        // Create any Draw2D figure as decoration for the connection
        //
        this.label = new draw2d.shape.basic.Label({
            text:attr && attr.weight || "1.0",
            color:"#0d0d0d",
            fontColor:"#0d0d0d",
            bgColor:"#f0f0f0"
        });

        // add the new decoration to the connection with a position locator.
        //
        // this.add(this.label, new draw2d.layout.locator.ManhattanMidpointLocator());
        this.add(this.label, new LeftEdgeLocator());
        this.label.setVisible(false);

        this.label.installEditor(new draw2d.ui.LabelInplaceEditor());
        this.installEditPolicy(new ShowWeightSelectionPolicy());
        this.setRouter(new draw2d.layout.connection.DirectRouter());
    },

    getWeight: function() {
        return this.label.getText();
    }
});