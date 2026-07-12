package fr.qmn.mamoucalendari.tasks;

public enum TasksPrefab {
    MEDICAL(
            "Rendez-vous chez le médecin",
            "Rendez-vous chez le kiné",
            "Prise de sang",
            "Rendez-vous chez le cardiologue",
            "Aller à la pharmacie"),
    FAMILY(
            "Visite de la famille",
            "Repas de famille"),
    FUN(
            "Amis du bon vieux temps",
            "Loto");
    private final String[] tasks;

    TasksPrefab(String... tasks) {
        this.tasks = tasks;
    }

    public String[] getTasks() {
        return tasks;
    }

    //Get tasks by category
    public static String[] getTasksByCategory(String category) {
        return switch (category) {
            case "Médical" -> MEDICAL.getTasks();
            case "Famille" -> FAMILY.getTasks();
            case "Loisir" -> FUN.getTasks();
            default -> new String[0];
        };
    }
}
