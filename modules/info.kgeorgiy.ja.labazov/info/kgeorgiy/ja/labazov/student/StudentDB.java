package info.kgeorgiy.ja.labazov.student;

import info.kgeorgiy.java.advanced.student.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

public class StudentDB implements AdvancedQuery {
    private static final Comparator<Student> STUDENT_COMPARATOR = Comparator
            .comparing(Student::getLastName, Comparator.reverseOrder())
            .thenComparing(Student::getFirstName, Comparator.reverseOrder())
            .thenComparingInt(Student::getId);

    private static String getFullName(final Student student) {
        return student.getFirstName() + " " + student.getLastName();
    }

    private static List<Group> getGroups(final Collection<Student> students, final Comparator<? super Student> comp) {
        return students.stream()
                .sorted(comp)
                .collect(Collectors.groupingBy(Student::getGroup, TreeMap::new, Collectors.toList()))
                .entrySet().stream()
                .map(entry -> new Group(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Group> getGroupsByName(final Collection<Student> students) {
        return getGroups(students, STUDENT_COMPARATOR);
    }

    @Override
    public List<Group> getGroupsById(final Collection<Student> students) {
        // :NOTE: Не нужно Comparator.comparing(Student::getId)
        return getGroups(students, Comparator.comparing(Student::getId));
    }

    private static GroupName getGroupByComp(final Collection<Student> students, final Comparator<Map.Entry<GroupName, List<Student>>> comp) {
        return students.stream()
                .collect(Collectors.groupingBy(Student::getGroup)).entrySet().stream()
                // :NOTE: Максимум
                .sorted(comp)
                .reduce((first, second) -> second)
                .map(Map.Entry::getKey).orElse(null);
    }

    private Comparator<Map.Entry<GroupName, List<Student>>> mapEntryValueComp(final ToIntFunction<Map.Entry<GroupName,
            List<Student>>> mapEntryIntExtractor) {
        return Comparator.comparingInt(mapEntryIntExtractor);
    }

    @Override
    public GroupName getLargestGroup(final Collection<Student> students) {
        // :NOTE: Память
        return getGroupByComp(students, mapEntryValueComp(entry -> entry.getValue().size())
                .thenComparing(Map.Entry::getKey));
    }

    @Override
    public GroupName getLargestGroupFirstName(final Collection<Student> students) {
        return getGroupByComp(students, mapEntryValueComp(entry -> getDistinctFirstNames(entry.getValue()).size())
                .thenComparing(Map.Entry::getKey, Comparator.reverseOrder())
        );
    }

    // :NOTE: Student
    private <T> List<T> getWithExtractor(final List<Student> students, final Function<? super Student, ? extends T> keyExtractor) {
        return students.stream().map(keyExtractor).collect(Collectors.toList());
    }

    @Override
    public List<String> getFirstNames(final List<Student> students) {
        return getWithExtractor(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(final List<Student> students) {
        return getWithExtractor(students, Student::getLastName);
    }

    @Override
    public List<GroupName> getGroups(final List<Student> students) {
        return getWithExtractor(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(final List<Student> students) {
        return getWithExtractor(students, StudentDB::getFullName);
    }

    @Override
    public Set<String> getDistinctFirstNames(final List<Student> students) {
        return students.stream().map(Student::getFirstName).collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public String getMaxStudentFirstName(final List<Student> students) {
        return students.stream().max(Comparator.comparingInt(Student::getId)).map(Student::getFirstName).orElse("");
    }

    private static List<Student> sortStudents(final Collection<Student> students, final Comparator<? super Student> comp) {
        return students.stream().sorted(comp).collect(Collectors.toList());
    }

    @Override
    public List<Student> sortStudentsById(final Collection<Student> students) {
        return sortStudents(students, Comparator.comparingInt(Student::getId));
    }

    @Override
    public List<Student> sortStudentsByName(final Collection<Student> students) {
        return sortStudents(students, STUDENT_COMPARATOR);
    }

    private static List<Student> findStudents(final Collection<Student> students, final Predicate<? super Student> pred) {
        return students.stream().filter(pred).sorted(STUDENT_COMPARATOR).collect(Collectors.toList());
    }

    private static <T> Predicate<Student> paramMatchesField(final Function<Student, T> func, final T param) {
        return student -> func.apply(student).equals(param);
    }

    // :NOTE: Дубли
    @Override
    public List<Student> findStudentsByFirstName(final Collection<Student> students, final String name) {
        return findStudents(students, paramMatchesField(Student::getFirstName, name));
    }

    @Override
    public List<Student> findStudentsByLastName(final Collection<Student> students, final String name) {
        return findStudents(students, paramMatchesField(Student::getLastName, name));
    }

    @Override
    public List<Student> findStudentsByGroup(final Collection<Student> students, final GroupName group) {
        return findStudents(students, paramMatchesField(Student::getGroup, group));
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(final Collection<Student> students, final GroupName group) {
        return students.stream()
                .filter(student -> student.getGroup() == group)
                .collect(Collectors.toMap(
                        Student::getLastName,
                        Student::getFirstName,
                        BinaryOperator.minBy(Comparator.naturalOrder())
                ));
    }

    @Override
    public String getMostPopularName(final Collection<Student> students) {
        return students.stream()
                .collect(Collectors.groupingBy(
                        Student::getFirstName,
                        Collectors.mapping(
                                Student::getGroup,
                                // :NOTE: Память
                                Collectors.collectingAndThen(Collectors.toSet(), Set::size)
                        )))
                .entrySet().stream()
                .max(Map.Entry.<String, Integer>comparingByValue().thenComparing(Map.Entry::getKey))
                .map(Map.Entry::getKey).orElse("");
    }

    private static <T> List<T> getByIndices(final Collection<Student> students, final int[] indices, final Function<Student, T> fun) {
        return Arrays.stream(indices).mapToObj(new ArrayList<>(students)::get).map(fun).collect(Collectors.toList());
    }

    @Override
    public List<String> getFirstNames(final Collection<Student> students, final int[] indices) {
        return getByIndices(students, indices, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(final Collection<Student> students, final int[] indices) {
        return getByIndices(students, indices, Student::getLastName);
    }

    @Override
    public List<GroupName> getGroups(final Collection<Student> students, final int[] indices) {
        return getByIndices(students, indices, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(final Collection<Student> students, final int[] indices) {
        return getByIndices(students, indices, StudentDB::getFullName);
    }
}
