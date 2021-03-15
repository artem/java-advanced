package info.kgeorgiy.ja.labazov.student;

import info.kgeorgiy.java.advanced.student.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

public class StudentDB implements AdvancedQuery {
    private static final Comparator<Student> compareByName = Comparator
            .comparing(Student::getLastName, Comparator.reverseOrder())
            .thenComparing(Student::getFirstName, Comparator.reverseOrder()).thenComparingInt(Student::getId);

    private static String getFullName(Student student) {
        return student.getFirstName() + " " + student.getLastName();
    }

    private List<Group> getGroups(Collection<Student> students, Comparator<? super Student> comp) {
        return students.stream().sorted(comp)
                .collect(Collectors.groupingBy(Student::getGroup, TreeMap::new, Collectors.toList()))
                .entrySet().stream().map(entry -> new Group(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Group> getGroupsByName(Collection<Student> students) {
        return getGroups(students, compareByName);
    }

    @Override
    public List<Group> getGroupsById(Collection<Student> students) {
        return getGroups(students, Comparator.comparing(Student::getId));
    }

    private GroupName getGroupByComp(Collection<Student> students, Comparator<Map.Entry<GroupName, List<Student>>> comp) {
        return students.stream().collect(Collectors.groupingBy(Student::getGroup)).entrySet().stream()
                .sorted(comp)
                .reduce((first, second) -> second)
                .map(Map.Entry::getKey).orElse(null);
    }

    private Comparator<Map.Entry<GroupName, List<Student>>> mapEntryValueComp(ToIntFunction<Map.Entry<GroupName,
            List<Student>>> mapEntryIntExtractor) {
        return Comparator.comparingInt(mapEntryIntExtractor);
    }

    @Override
    public GroupName getLargestGroup(Collection<Student> students) {
        return getGroupByComp(students, mapEntryValueComp(entry -> entry.getValue().size())
                .thenComparing(Map.Entry::getKey));
    }

    @Override
    public GroupName getLargestGroupFirstName(Collection<Student> students) {
        return getGroupByComp(students, mapEntryValueComp(entry -> getDistinctFirstNames(entry.getValue()).size())
                .thenComparing(Map.Entry::getKey, Comparator.reverseOrder())
        );
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

    private <T> Predicate<Student> paramMatchesField(Function<Student, T> func, T param) {
        return student -> func.apply(student).equals(param);
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return findStudents(students, paramMatchesField(Student::getFirstName, name));
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return findStudents(students, paramMatchesField(Student::getLastName, name));
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, GroupName group) {
        return findStudents(students, paramMatchesField(Student::getGroup, group));
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, GroupName group) {
        return students.stream().filter(student -> student.getGroup() == group)
                .collect(Collectors.toMap(Student::getLastName,Student::getFirstName,
                        BinaryOperator.minBy(Comparator.naturalOrder())));
    }

    @Override
    public String getMostPopularName(Collection<Student> students) {
        return students.stream()
                .collect(Collectors.groupingBy(Student::getFirstName, Collectors
                        .mapping(Student::getGroup, Collectors.collectingAndThen(Collectors.toSet(), Set::size))))
                .entrySet().stream()
                .max(Map.Entry.<String, Integer>comparingByValue().thenComparing(Map.Entry::getKey))
                .map(Map.Entry::getKey).orElse("");
    }

    private <T> List<T> getByIndices(Collection<Student> students, int[] indices, Function<Student, T> fun) {
        return Arrays.stream(indices).mapToObj(new ArrayList<>(students)::get).map(fun).collect(Collectors.toList());
    }

    @Override
    public List<String> getFirstNames(Collection<Student> students, int[] indices) {
        return getByIndices(students, indices, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(Collection<Student> students, int[] indices) {
        return getByIndices(students, indices, Student::getLastName);
    }

    @Override
    public List<GroupName> getGroups(Collection<Student> students, int[] indices) {
        return getByIndices(students, indices, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(Collection<Student> students, int[] indices) {
        return getByIndices(students, indices, StudentDB::getFullName);
    }
}
