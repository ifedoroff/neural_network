var MidLineLocator = draw2d.layout.locator.Locator.extend({
    init: function (attr, setter, getter) {
        this._super(attr, setter, getter);
    },

    relocate: function (index, figure) {
        // just repaint the child to update the SVG related to the new location
        // of the parent.
        var parentBox = figure.getParent().getOuterBoundingBox();
        figure.setY(parent.y + (parentBox.getHeight() - figure.getHeight()) /2 );
        this._super(index, figure);
    }
});