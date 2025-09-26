public class HomeControllerTest {

    @Test
    void testAddSinglePlayer() {
        HelloController controller = new HelloController();
        controller.addPlayer("Alice", "Lock Picker");
        assertEquals(1, controller.getPlayers().size());
    }

    @Test
    void testAddMultiplePlayers() {
        HelloController controller = new HelloController();
        controller.addPlayer("Alice", "Lock Picker");
        controller.addPlayer("Bob", "Dystopian Survivor");
        assertEquals(2, controller.getPlayers().size());
    }

    @Test
    void testUpdateExistingPlayerAchievement() {
        HelloController controller = new HelloController();
        controller.addPlayer("Alice", "Lock Picker");
        controller.addPlayer("Alice", "Master Hacker");

        List<Players> players = controller.getPlayers();
        Players alice = players.stream().filter(p -> p.getUsername().equals("Alice")).findFirst().orElse(null);
        assertNotNull(alice);
        assertEquals("Master Hacker", alice.getAchievement());
    }

    @Test
    void testHandleTiePlayers() {
        HelloController controller = new HelloController();
        controller.addPlayer("Alice", "Lock Picker");
        controller.addPlayer("Bob", "Lock Picker");
        assertEquals(2, controller.getPlayers().size());
    }

    @Test
    void testRejectNullOrEmptyPlayerName() {
        HelloController controller = new HelloController();
        assertThrows(IllegalArgumentException.class, () -> controller.addPlayer(null, "Achievement"));
        assertThrows(IllegalArgumentException.class, () -> controller.addPlayer("", "Achievement"));
    }

    @Test
    void testControllerInitiallyEmpty() {
        HelloController controller = new HelloController();
        assertTrue(controller.isEmpty());
    }
}