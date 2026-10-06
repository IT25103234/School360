# Observer pattern: integrated School360 workflows

The original teacher-announcement console example is now also integrated into the live application. See [the complete design-patterns report](design-patterns.md) for all six modules, file changes, participant explanations, UML/Mermaid diagrams, tests and assignment screenshots.

`NotificationSubject` and `NotificationObserver` are unchanged. `AnnouncementSubject` now inherits shared subscription mechanics from `AbstractNotificationSubject`. `StudentAnnouncementObserver` retains its original console constructor and adds a repository-based constructor for actual student inbox delivery. Library, enrollment and support reuse the same interfaces and shared mechanics.

## Retained console demonstration

`src/test/java/com/school360/pattern/observer/AnnouncementObserverDemo.java` is unchanged. Run its main method with the test classpath, or after `mvn test-compile`:

```powershell
java -cp 'target/test-classes;target/classes' com.school360.pattern.observer.AnnouncementObserverDemo
```

It adds Amali and Nimal, publishes an announcement, removes Nimal and publishes an update. Expected output:

```text
Amali received: Announcement: Science meeting - Meet in Room 5 at 10 AM.
Nimal received: Announcement: Science meeting - Meet in Room 5 at 10 AM.
Amali received: Announcement: Science meeting update - Please bring your notebook.
```

The console demo remains in-memory. The real teacher controller uses existing student records and stores ANNOUNCEMENT inbox notifications. The end-to-end test verifies that actual persistence path. Subjects are event-local, synchronous objects, not shared mutable Spring singletons. No persistent subscription table or automatic database-change listener was added.
