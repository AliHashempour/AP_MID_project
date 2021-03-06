/**
 * The type Role.
 *
 * @author ALi.Hashempour
 */
public abstract class Role {

    int healthBar;


    public int getHealthBar() {
        return healthBar;
    }

    public void setHealthBar(int healthBar) {
        this.healthBar = healthBar;
    }

    public abstract void decreaseHealth();

    public abstract void increaseHealth();


}
