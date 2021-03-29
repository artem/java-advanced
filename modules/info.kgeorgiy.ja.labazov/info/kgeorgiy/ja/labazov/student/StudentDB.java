package info.kgeorgiy.ja.labazov.student;

import info.kgeorgiy.java.advanced.student.AdvancedQuery;
import info.kgeorgiy.java.advanced.student.Group;
import info.kgeorgiy.java.advanced.student.GroupName;
import info.kgeorgiy.java.advanced.student.Student;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class StudentDB implements AdvancedQuery {
    private static final Comparator<Student> STUDENT_COMPARATOR = Comparator
            .comparing(Student::getLastName, Comparator.reverseOrder())
            .thenComparing(Student::getFirstName, Comparator.reverseOrder())
            .thenComparingInt(Student::getId);
    private static final Comparator<Student> ID_COMPARATOR = Comparator.comparingInt(Student::getId);

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
        return getGroups(students, ID_COMPARATOR);
    }

    private static <R extends Comparable<? super R>, T> T getGroup(final Collection<Student> students, Collector<Student, ?, R> col,
                                                                                                 Comparator<? super T> comp, Function<Student, T> extr, T def) {
        return students.stream()
                .collect(Collectors.groupingBy(extr, col)).entrySet().stream()
                .max(Entry.<T, R>comparingByValue().thenComparing(Entry::getKey, comp))
                .map(Entry::getKey).orElse(def);
    }

    @Override
    public GroupName getLargestGroup(final Collection<Student> students) {
        return getGroup(students, Collectors.counting(), Comparator.naturalOrder(), Student::getGroup, null);
    }

    private static <T> Collector<T, ?, Integer> distinctAmountByExtr(Function<T, ?> extr) {
        return Collectors.mapping(extr, Collectors.collectingAndThen(Collectors.toSet(), Set::size));
    }

    @Override
    public GroupName getLargestGroupFirstName(final Collection<Student> students) {
        // Explicit type arguments are required for successful type inference
        return StudentDB.<Integer, GroupName>getGroup(students, distinctAmountByExtr(Student::getFirstName),
                Comparator.reverseOrder(), Student::getGroup, null);
    }

    private static <T, U> List<T> getWithExtractor(final List<U> entries, final Function<? super U, ? extends T> keyExtractor) {
        return entries.stream().map(keyExtractor).collect(Collectors.toList());
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
        return students.stream().max(ID_COMPARATOR).map(Student::getFirstName).orElse("");
    }

    private static <T> List<T> sortStudents(final Collection<T> entries, final Comparator<? super T> comp) {
        return entries.stream().sorted(comp).collect(Collectors.toList());
    }

    @Override
    public List<Student> sortStudentsById(final Collection<Student> students) {
        return sortStudents(students, ID_COMPARATOR);
    }

    @Override
    public List<Student> sortStudentsByName(final Collection<Student> students) {
        return sortStudents(students, STUDENT_COMPARATOR);
    }

    private static <T> List<Student> findStudents(final Collection<Student> students,
                                                  final Function<Student, T> func, final T param) {
        return students.stream()
                .filter(paramMatchesField(func, param))
                .sorted(STUDENT_COMPARATOR)
                .collect(Collectors.toList());
    }

    private static <T> Predicate<Student> paramMatchesField(final Function<Student, T> func, final T param) {
        return student -> func.apply(student).equals(param);
    }

    @Override
    public List<Student> findStudentsByFirstName(final Collection<Student> students, final String name) {
        return findStudents(students, Student::getFirstName, name);
    }

    @Override
    public List<Student> findStudentsByLastName(final Collection<Student> students, final String name) {
        return findStudents(students, Student::getLastName, name);
    }

    @Override
    public List<Student> findStudentsByGroup(final Collection<Student> students, final GroupName group) {
        return findStudents(students, Student::getGroup, group);
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(final Collection<Student> students, final GroupName group) {
        return students.stream()
                .filter(paramMatchesField(Student::getGroup, group))
                .collect(Collectors.toMap(
                        Student::getLastName,
                        Student::getFirstName,
                        BinaryOperator.minBy(Comparator.naturalOrder())
                ));
    }

    @Override
    public String getMostPopularName(final Collection<Student> students) {
        // Explicit type arguments are required for successful type inference
        return StudentDB.<Integer, String>getGroup(students, distinctAmountByExtr(Student::getGroup),
                Comparator.naturalOrder(), Student::getFirstName, "");
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
