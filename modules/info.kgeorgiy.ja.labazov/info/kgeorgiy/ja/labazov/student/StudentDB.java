package info.kgeorgiy.ja.labazov.student;

import info.kgeorgiy.java.advanced.student.Group;
import info.kgeorgiy.java.advanced.student.GroupName;
import info.kgeorgiy.java.advanced.student.GroupQuery;
import info.kgeorgiy.java.advanced.student.Student;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class StudentDB implements GroupQuery {
    private static final Comparator<Student> compareByName = Comparator
            .comparing(Student::getLastName, Comparator.reverseOrder())
            .thenComparing(Student::getFirstName, Comparator.reverseOrder()).thenComparingInt(Student::getId);

    private static String getFullName(Student student) {
        return student.getFirstName() + " " + student.getLastName();
    }

    @Override
    public List<Group> getGroupsByName(Collection<Student> students) {
        return students.stream().sorted(compareByName)
                .collect(Collectors.groupingBy(Student::getGroup, TreeMap::new, Collectors.toList()))
                .entrySet().stream().map(entry -> new Group(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Group> getGroupsById(Collection<Student> students) {
        return students.stream().sorted(Comparator.comparing(Student::getId))
                .collect(Collectors.groupingBy(Student::getGroup, TreeMap::new, Collectors.toList()))
                .entrySet().stream().map(entry -> new Group(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public GroupName getLargestGroup(Collection<Student> students) {
        return students.stream().collect(Collectors.groupingBy(Student::getGroup)).entrySet().stream()
                .sorted(Comparator.<Map.Entry<GroupName, List<Student>>>comparingInt(entry -> entry.getValue().size())
                        .thenComparing(Map.Entry::getKey))
                .reduce((first, second) -> second)
                .map(Map.Entry::getKey).orElse(null);
    }

    @Override
    public GroupName getLargestGroupFirstName(Collection<Student> students) {
        return students.stream().collect(Collectors.groupingBy(Student::getGroup)).entrySet().stream()
                .sorted(Comparator.<Map.Entry<GroupName, List<Student>>>comparingInt(entry -> getDistinctFirstNames(entry.getValue()).size())
                        .thenComparing(Map.Entry::getKey, Comparator.reverseOrder()))
                .reduce((first, second) -> second)
                .map(Map.Entry::getKey).orElse(null);
    }

    private <T> List<T> getWithExtractor(List<Student> students, Function<? super Student, ? extends T> keyExtractor) {
        return students.stream().map(keyExtractor).collect(Collectors.toList());
    }

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return getWithExtractor(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return getWithExtractor(students, Student::getLastName);
    }

    @Override
    public List<GroupName> getGroups(List<Student> students) {
        return getWithExtractor(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return getWithExtractor(students, StudentDB::getFullName);
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return students.stream().map(Student::getFirstName).collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public String getMaxStudentFirstName(List<Student> students) {
        return students.stream().max(Comparator.comparingInt(Student::getId)).map(Student::getFirstName).orElse("");
    }

    private List<Student> sortStudents(Collection<Student> students, Comparator<? super Student> comp) {
        return students.stream().sorted(comp).collect(Collectors.toList());
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return sortStudents(students, Comparator.comparingInt(Student::getId));
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sortStudents(students, compareByName);
    }

    private List<Student> findStudents(Collection<Student> students, Predicate<? super Student> pred) {
        return students.stream().filter(pred).sorted(compareByName).collect(Collectors.toList());
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return findStudents(students, student -> student.getFirstName().equals(name));
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return findStudents(students, student -> student.getLastName().equals(name));
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, GroupName group) {
        return findStudents(students, student -> student.getGroup().equals(group));
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, GroupName group) {
        return students.stream().filter(student -> student.getGroup() == group)
                .sorted(Comparator.comparing(Student::getFirstName, Comparator.reverseOrder()))
                .collect(Collectors.groupingBy(Student::getLastName,
                        Collectors.reducing(null, Student::getFirstName, (a,b) -> b)));
    }
}
