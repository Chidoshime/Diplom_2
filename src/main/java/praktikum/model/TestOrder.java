package praktikum.model;


public class TestOrder {

    private String[] ingredients;

    public TestOrder(String[] ingredients){
        this.ingredients = ingredients;
    }

    public String[] getIngredients() {
        return ingredients;
    }

    public void setIngredients(String[] ingredients) {
        this.ingredients = ingredients;
    }
}