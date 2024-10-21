package game;

import game.object.Pathfinder;
import game.object.entity.Actor;
import game.object.entity.EntityType;
import game.ui.Message;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.List;

public class Mover {
    private final Game game;

    public Mover(Game game) {
        this.game = game;
    }

    public void handleManualMovement(int dx, int dy, Actor actor) {
        // Calculate the new player position
        int newX = actor.getX() + dx;
        int newY = actor.getY() + dy;

        // Check for valid movement and update player position
        if (newX >= 0 && newX < Constants.GAME_TILE_NUM && newY >= 0 && newY < Constants.GAME_TILE_NUM &&
                (this.game.getGameState().getCurrentDungeon().getTile(newX, newY).getType() == EntityType.FLOOR ||
                        this.game.getGameState().getCurrentDungeon().getTile(newX, newY).getType() == EntityType.EXIT ||
                        this.game.getGameState().getCurrentDungeon().getTile(newX, newY).getType() == EntityType.DOOR)) {
            actor.move(dx, dy);
        }

        // Check for reaching the exit and advance to the next level
        if (actor.getX() == this.game.getGameState().getCurrentDungeon().getExitX()
                && actor.getY() == this.game.getGameState().getCurrentDungeon().getExitY()) {
            game.advanceToNextLevel();
        }
    }

    // Handle movement with pathfinder
    public void handleAutoMovement(MouseEvent e, Actor actor) {
        // Convert mouse coordinates to grid coordinates
        int gridX = e.getX() / Constants.GAME_TILE_SIZE;
        int gridY = (e.getY() - Constants.Y_OFFSET) / Constants.GAME_TILE_SIZE;

        if (this.game.getGameState().getCurrentDungeon().getTile(gridX, gridY).getType() != EntityType.FLOOR) { // Cannot move to non-floor tiles
            this.game.getGameState().setMessage(new Message("It ain't that easy", this.game));
            this.game.getGameState().getMessage().display(750);
            return;
        }

        Pathfinder pathfinder = new Pathfinder(this.game.getGameState().getCurrentDungeon());
        java.util.List<Point> path = pathfinder.findPath(actor.getX(), actor.getY(), gridX, gridY);
        if (path == null) return;
        animateAutoMovement(path, actor);
    }

    private void animateAutoMovement(List<Point> path, Actor actor) {
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
