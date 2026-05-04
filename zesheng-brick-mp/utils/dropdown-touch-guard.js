const DEFAULT_MOVE_THRESHOLD = 18;

function getTouchPoint(event) {
    const touch = (event && event.touches && event.touches[0])
        || (event && event.changedTouches && event.changedTouches[0]);
    if (!touch) {
        return null;
    }
    return {
        x: Number(touch.pageX) || 0,
        y: Number(touch.pageY) || 0,
    };
}

function initTouchGuard(context) {
    context._dropdownTouchMoved = false;
    context._dropdownTouchStartX = 0;
    context._dropdownTouchStartY = 0;
}

function onTouchStart(context, event) {
    const point = getTouchPoint(event);
    context._dropdownTouchMoved = false;
    if (!point) {
        return;
    }
    context._dropdownTouchStartX = point.x;
    context._dropdownTouchStartY = point.y;
}

function onTouchMove(context, event, threshold = DEFAULT_MOVE_THRESHOLD) {
    const point = getTouchPoint(event);
    if (!point) {
        context._dropdownTouchMoved = true;
        return;
    }
    const deltaX = Math.abs(point.x - (context._dropdownTouchStartX || 0));
    const deltaY = Math.abs(point.y - (context._dropdownTouchStartY || 0));
    if (deltaX > threshold || deltaY > threshold) {
        context._dropdownTouchMoved = true;
    }
}

function shouldBlockSelect(context) {
    return !!context._dropdownTouchMoved;
}

function resetMoveState(context) {
    context._dropdownTouchMoved = false;
}

module.exports = {
    DEFAULT_MOVE_THRESHOLD,
    initTouchGuard,
    onTouchStart,
    onTouchMove,
    shouldBlockSelect,
    resetMoveState,
};
