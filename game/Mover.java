package game;

import game.object.Pathfinder;
import game.object.entity.DynamicEntity;
import game.object.entity.EntityType;
import game.ui.Message;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class Mover {
    private final Game game;

    public Mover(Game game) {
        this.game = game;
    }

    public void moveBy(DynamicEntity actor, int dx, int dy) {
        int newX = actor.getX() + dx;
        int newY = actor.getY() + dy;

        if (newX >= 0 && newX < Constants.GAME_TILE_NUM && newY >= 0 && newY < Constants.GAME_TILE_NUM &&
                (this.game.getGameState().getCurrentDungeon().getTile(newX, newY).type() == EntityType.FLOOR ||
                        this.game.getGameState().getCurrentDungeon().getTile(newX, newY).type() == EntityType.EXIT ||
                        this.game.getGameState().getCurrentDungeon().getTile(newX, newY).type() == EntityType.DOOR)) {
            actor.move(dx, dy);
        }
    }

    public void moveTo(DynamicEntity actor, int targetX, int targetY) {
        if (this.game.getGameState().getCurrentDungeon().getTile(targetX, targetY).type() != EntityType.FLOOR) { // Cannot move to non-floor tiles
            this.game.getGameState().setMessage(new Message("It ain't that easy", this.game));
            this.game.getGameState().getMessage().display(750);
            return;
        }

        Pathfinder pathfinder = new Pathfinder(this.game.getGameState().getCurrentDungeon());
        java.util.List<Point> path = pathfinder.findPath(actor.getX(), actor.getY(), targetX, targetY);
        if (path == null) return;
        animateAutoMovement(actor, path);
    }

    private void animateAutoMovement(DynamicEntity actor, List<Point> path) {
        Timer timer = new Timer(Constants.GAME_AUTO_MOVEMENT_DELAY, null);
        this.game.getGameState().setMovementInProgress(true);

        timer.addActionListener(new ActionListener() {
            int index = 1; // Start at 1 to skip the player's current position

            @Override
            public void actionPerformed(ActionEvent event) {
                if (index < path.size()) {
                    Point position = path.get(index);

                    actor.move(position.x - actor.getX(), position.y - actor.getY());

                    index++;
                } else {
                    timer.stop();
                    Mover.this.game.getGameState().setMovementInProgress(false);
                }
            }

        });
        timer.start();
    }
}
