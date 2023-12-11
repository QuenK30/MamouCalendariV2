package fr.qmn.mamoucalendari.tasks;

public enum TasksPrefab {
    MEDICAL(
            "Rendez-vous médical",
            "Examen dentaire",
            "Examen ophtalmologique",
            "Examen radiologique",
            "Examen Divers"),
    ADMINISTRATIVE(
            "Rendez-vous administratif",
            "Allez à la banque",
            "Rendez-vous juridique",
            "Rendez-vous avec un avocat",
            "Rendez-vous avec un conseiller bancaire"),
    FAMILY(
            "Repas de famille",
            "Anniversaire",
            "Mariage",
            "Baptême",
            "Cérémonie"),
    FUN(
            "Sortie entre amis",
            "Cinéma",
            "Théâtre",
            "Sophrologie",
            "Couture");
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
            case "Administratif" -> ADMINISTRATIVE.getTasks();
            case "Famille" -> FAMILY.getTasks();
            case "Loisir" -> FUN.getTasks();
            default -> new String[0];
        };
    }
}
