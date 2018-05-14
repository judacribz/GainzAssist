package ca.judacribz.gainzassist.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Exercise {

    // Global Vars
    // --------------------------------------------------------------------------------------------
    private String name, type, equipment;
    private ArrayList<Set> sets;
    // --------------------------------------------------------------------------------------------

    // ######################################################################################### //
    // Exercise Constructors                                                                     //
    // ######################################################################################### //
    // ######################################################################################### //
    /* Required empty constructor for firebase */
    public Exercise() {
    }

    public Exercise(String name, String type, String equipment, ArrayList<Set> sets) {
        this.name      = name;
        this.type      = type;
        this.equipment = equipment;
        this.sets      = sets;
    }
    // ============================================================================================

    // Getters and setters
    // ============================================================================================
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public ArrayList<Set> getSets() {
        return sets;
    }

    public void setSets(ArrayList<Set> sets) {
        this.sets = sets;
    }
    // ============================================================================================


    /* Helper function used to store Exercise information in the firebase db */
    Map<String, Object> toMap() {
        Map<String, Object> exercise = new HashMap<>();

        exercise.put("type",      type);
        exercise.put("equipment", equipment);

        Map<String, Object> setMap = new HashMap<>();

        for (Set set: sets) {
            setMap.put(String.valueOf(set.getSetNumber()), set.toMap());
        }

        exercise.put("sets", setMap);

        return exercise;
    }
}
