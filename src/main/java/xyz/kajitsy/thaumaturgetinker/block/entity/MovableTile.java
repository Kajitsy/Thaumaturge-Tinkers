package xyz.kajitsy.thaumaturgetinker.block.entity;
/** Native equivalent of build164's movable tile contract for locomotive passengers. */
public interface MovableTile {
 default boolean prepareToMove(){return true;}
 default void doneMoving(){}
}
