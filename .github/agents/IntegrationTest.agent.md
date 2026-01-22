---
description: 'Description of the custom chat mode.'
tools: []
---
Define the purpose of this chat mode and how AI should behave: response style, available tools, focus areas, and any mode-specific instructions or constraints.
---
applyTo: '**'
description: 'description'
---
Provide project context and coding guidelines that AI should follow when generating code, answering questions, or reviewing changes.

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
public class LessonControllerIntegrationTest extends BaseIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private LessonRepository lessonRepository;

	@Autowired
	private StudentRepository studentRepository;

	@Autowired
	private TeacherRepository teacherRepository;

	@BeforeEach
	void cleanup() {
		lessonRepository.deleteAll();
		studentRepository.deleteAll();
		teacherRepository.deleteAll();
	}

	@Test
	void getAll_returnsLessonsWithActiveParticipants() throws Exception {
		Teacher savedTeacher = teacherRepository.save(createTeacherWithId(null));
		Student student = createStudentWithId(null, null);
		student.setTeacher(savedTeacher);
		Student savedStudent = studentRepository.save(student);

		LocalDateTime date1 = LocalDateTime.now().plusDays(1);
		LocalDateTime date2 = date1.plusDays(1);

		Lesson l1 = new Lesson();
		l1.setTeacher(savedTeacher);
		l1.setStudent(savedStudent);
		l1.setDate(date1);
		Lesson l2 = new Lesson();
		l2.setTeacher(savedTeacher);
		l2.setStudent(savedStudent);
		l2.setDate(date2);

		lessonRepository.save(l1);
		lessonRepository.save(l2);

		// when / then
		mockMvc.perform(get("/api/v1/lessons"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].teacher.id").isNotEmpty())
				.andExpect(jsonPath("$[0].student.id").isNotEmpty());
	}

	@Test
	void addLesson_createsLessonAndReturnsCreated() throws Exception {
		// given
		Teacher savedTeacher = teacherRepository.save(createTeacherWithId(null));
		Student student = createStudentWithId(null, null);
		student.setTeacher(savedTeacher);
		Student savedStudent = studentRepository.save(student);

		LocalDateTime date = LocalDateTime.now().plusDays(2);

		CreateLessonCommand cmd = new CreateLessonCommand(savedTeacher.getId(), savedStudent.getId(), date);
		String body = toJson(cmd);

		// when / then
		mockMvc.perform(post("/api/v1/lessons")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isCreated())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.teacher.id").value(savedTeacher.getId().intValue()))
				.andExpect(jsonPath("$.student.id").value(savedStudent.getId().intValue()))
				.andExpect(jsonPath("$.date").isNotEmpty());

		// and persisted
		List<Lesson> all = lessonRepository.findLessonsWithActiveParticipants();
		assertThat(all).hasSize(1);
		Lesson persisted = all.get(0);
		assertThat(persisted.getTeacher().getId()).isEqualTo(savedTeacher.getId());
		assertThat(persisted.getStudent().getId()).isEqualTo(savedStudent.getId());
		assertThat(persisted.getDate()).isNotNull();
	}

	@Test
	void deleteLesson_removesLesson() throws Exception {
		// given
		Teacher savedTeacher = teacherRepository.save(createTeacherWithId(null));
		Student student = createStudentWithId(null, null);
		student.setTeacher(savedTeacher);
		Student savedStudent = studentRepository.save(student);

		Lesson lesson = new Lesson();
		lesson.setTeacher(savedTeacher);
		lesson.setStudent(savedStudent);
		lesson.setDate(LocalDateTime.now().plusDays(3));
		Lesson saved = lessonRepository.save(lesson);

		// when
		mockMvc.perform(delete("/api/v1/lessons/{id}", saved.getId()))
				.andExpect(status().isNoContent());

		// then
		Optional<Lesson> maybe = lessonRepository.findById(saved.getId());
		assertThat(maybe).isEmpty();
	}

@Testcontainers
public class CodingApiTestHelper {

	public static final LocalDateTime CURRENT_DATE = LocalDateTime.of(2020, 1, 1, 12, 0);

	public Teacher createTeacherWithId(Long id){
		Teacher teacher = new Teacher();
		teacher.setId(id);
		teacher.setFirstName("John");
		teacher.setLastName("Doe");
		teacher.setLanguages(Set.of(Language.EN, Language.PL));
		return teacher;
	}

	public Student createStudentWithId(Long studentId, Long teacherId){
		Student student = new Student();
		student.setId(studentId);
		student.setFirstName("Jane");
		student.setLastName("Smith");
		student.setLanguage(Language.EN);
		student.setTeacher(createTeacherWithId(teacherId));
		return student;
	}

	public Lesson createLessonWithId(Long id, Long studentId, Long teacherId) {
		Lesson lesson = new Lesson();
		lesson.setId(id);
		lesson.setTeacher(createTeacherWithId(teacherId));
		lesson.setStudent(createStudentWithId(studentId, teacherId));
		lesson.setDate(CURRENT_DATE);
		return lesson;
	}

	public String toJson(Object value) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.registerModule(new JavaTimeModule());
			return mapper.writeValueAsString(value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	public String readJson(String path) throws Exception {
		return Files.readString(Path.of(path));
	}

@ExtendWith(MockitoExtension.class)
class LessonServiceTest extends CodingApiTestHelper {

	@InjectMocks
	private LessonService classUnderTest;

	@Mock
	private LessonRepository lessonRepository;

	@Mock
	private StudentRepository studentRepository;

	@Mock
	private TeacherRepository teacherRepository;

	@Mock
	private CommonValidator commonValidator;

	@Captor
	ArgumentCaptor<Lesson> lessonCaptor;

	@Test
	void getAll_shouldReturnMappedDTOs() {
		//given
		Lesson lesson1 = createLessonWithId(1L, 1L, 1L);
		Lesson lesson2 = createLessonWithId(2L, 2L, 2L);
		List<LessonDTO> expected = List.of(LessonMapper.toDto(lesson1), LessonMapper.toDto(lesson2));

		when(lessonRepository.findLessonsWithActiveParticipants()).thenReturn(List.of(lesson1, lesson2));

		//when
		List<LessonDTO> result = classUnderTest.getAll();

		//then
		assertThat(result).hasSize(2);
		assertThat(result)
				.usingRecursiveComparison()
				.isEqualTo(expected);

		//bean interaction
		verify(lessonRepository).findLessonsWithActiveParticipants();
	}

	@Test
	void addLesson_shouldSaveAndReturnDto() {
		//given
		long teacherId = 2L;
		long studentId = 1L;
		long expectedId = 10L;
		CreateLessonCommand cmd = new CreateLessonCommand(studentId, teacherId, CURRENT_DATE.plusDays(1));
		Student student = createStudentWithId(studentId, studentId);
		Teacher teacher = createTeacherWithId(teacherId);

		when(studentRepository.findByIdWithLock(studentId)).thenReturn(Optional.of(student));
		when(teacherRepository.findByIdWithLock(teacherId)).thenReturn(Optional.of(teacher));
		doNothing().when(commonValidator).validate(teacher, student, cmd.date());
		when(lessonRepository.save(any(Lesson.class))).thenAnswer(args -> {
			Lesson lesson = args.getArgument(0);
			lesson.setId(expectedId);
			return lesson;
		});

		//when
		LessonDTO result = classUnderTest.addLesson(cmd);

		//then
		verify(studentRepository).findByIdWithLock(studentId);
		verify(teacherRepository).findByIdWithLock(teacherId);
		verify(commonValidator).validate(teacher, student, cmd.date());

		//verification data before execute
		verify(lessonRepository).save(lessonCaptor.capture());
		Lesson saved = lessonCaptor.getValue();
		assertThat(saved.getStudent()).isEqualTo(student);
		assertThat(saved.getTeacher()).isEqualTo(teacher);
		assertThat(saved.getDate()).isEqualTo(cmd.date());

		//result
		assertThat(result.id()).isEqualTo(expectedId);
		assertThat(result.student().id()).isEqualTo(cmd.studentId());
		assertThat(result.teacher().id()).isEqualTo(cmd.teacherId());
		assertThat(result.date()).isEqualTo(cmd.date());
	}

	@Test
	void addLesson_shouldThrowWhenLockFails() {
		//given
		CreateLessonCommand cmd = new CreateLessonCommand(1L, 2L, CURRENT_DATE);
		Student student = createStudentWithId(1L, 1L);
		Teacher teacher = createTeacherWithId(2L);

		when(studentRepository.findByIdWithLock(1L)).thenReturn(Optional.of(student));
		when(teacherRepository.findByIdWithLock(2L)).thenReturn(Optional.of(teacher));
		when(lessonRepository.save(any(Lesson.class)))
				.thenThrow(new PessimisticLockingFailureException("Lock failed"));

		//then
		assertThatThrownBy(() -> classUnderTest.addLesson(cmd))
				.isInstanceOf(CodingApiException.class)
				.hasMessageContaining("Could not acquire lock");

		verify(commonValidator).validate(teacher, student, cmd.date());
		verify(studentRepository).findByIdWithLock(1L);
		verify(teacherRepository).findByIdWithLock(2L);

		verify(lessonRepository).save(lessonCaptor.capture());
		Lesson saved = lessonCaptor.getValue();
		assertThat(saved).isNotNull();
		assertThat(saved.getStudent().getId()).isEqualTo(cmd.studentId());
		assertThat(saved.getTeacher().getId()).isEqualTo(cmd.teacherId());
		assertThat(saved.getDate()).isEqualTo(cmd.date());
	}

	@Test
	void deleteLesson_shouldDeleteWhenValid() {
		//given
		Lesson lesson = createLessonWithId(1L, 1L, 1L);
		when(lessonRepository.findById(1L)).thenReturn(Optional.of(lesson));

		//when
		classUnderTest.deleteLesson(1L);

		//then
		verify(commonValidator).validateLessonDateNotInThePast(lesson.getDate());
		verify(lessonRepository).delete(lesson);

		verify(lessonRepository).delete(lessonCaptor.capture());
		Lesson deleted = lessonCaptor.getValue();
		assertThat(deleted).isNotNull();
		assertThat(deleted.getId()).isEqualTo(1L);
	}

	@Test
	void deleteLesson_shouldThrowWhenNotFound() {
		//given
		when(lessonRepository.findById(1L)).thenReturn(Optional.empty());

		//then
		assertThatThrownBy(() -> classUnderTest.deleteLesson(1L))
				.isInstanceOf(CodingApiException.class)
				.hasMessageContaining("Lesson not found");

		verify(lessonRepository).findById(1L);
		verify(commonValidator, never()).validateLessonDateNotInThePast(any(LocalDateTime.class));
		verify(lessonRepository, never()).delete(any(Lesson.class));
	}

	@Test
	void changeDate_shouldChangeAndValidateDateSuccessfully() {
		//given
		Long lessonId = 1L;
		LocalDateTime newDate = CURRENT_DATE.plusDays(5);
		ChangeLessonDateCommand cmd = new ChangeLessonDateCommand(newDate);

		Lesson lesson = createLessonWithId(lessonId, 1L, 1L);

		when(lessonRepository.findByIdWithLock(lessonId)).thenReturn(Optional.of(lesson));

		when(studentRepository.findByIdWithLock(any(Long.class))).thenReturn(Optional.of(lesson.getStudent()));
		when(teacherRepository.findByIdWithLock(any(Long.class))).thenReturn(Optional.of(lesson.getTeacher()));
		when(lessonRepository.save(any(Lesson.class))).thenAnswer(args -> args.getArgument(0));

		//when
		LessonDTO result = classUnderTest.changeAndValidateDate(lessonId, cmd);

		//then
		assertThat(result.date()).isEqualTo(newDate);
		assertThat(result.id()).isEqualTo(lessonId);

		verify(commonValidator).validate(lesson.getTeacher(), lesson.getStudent(), newDate);
		verify(studentRepository).findByIdWithLock(lessonId);
		verify(teacherRepository).findByIdWithLock(lessonId);

		verify(lessonRepository).save(lessonCaptor.capture());
		Lesson saved = lessonCaptor.getValue();
		assertThat(saved).isNotNull();
		assertThat(saved.getId()).isEqualTo(lessonId);
		assertThat(saved.getDate()).isEqualTo(newDate);
	}

	@Test
	void changeAndValidateDate_shouldThrowWhenLessonNotFound() {
		//given
		when(lessonRepository.findByIdWithLock(1L)).thenReturn(Optional.empty());

		//then
		assertThatThrownBy(() -> classUnderTest.changeAndValidateDate(1L, new ChangeLessonDateCommand(CURRENT_DATE.plusDays(2))))
				.isInstanceOf(CodingApiException.class)
				.hasMessageContaining("Lesson not found");
		verify(lessonRepository).findByIdWithLock(1L);
	}

	@Test
	void changeAndValidateDate_shouldThrowOnLockTimeout() {
		//given
		Long lessonId = 1L;
		LocalDateTime newDate = CURRENT_DATE.plusDays(2);
		Lesson lesson = createLessonWithId(1L, 1L , 1L);
		when(lessonRepository.findByIdWithLock(lessonId)).thenReturn(Optional.of(lesson));
		when(studentRepository.findByIdWithLock(any(Long.class))).thenReturn(Optional.of(lesson.getStudent()));
		when(teacherRepository.findByIdWithLock(any(Long.class))).thenReturn(Optional.of(lesson.getTeacher()));
		when(lessonRepository.save(lesson)).thenThrow(new LockTimeoutException("Timeout", null));

		//then
		assertThatThrownBy(() -> classUnderTest.changeAndValidateDate(lessonId, new ChangeLessonDateCommand(newDate)))
				.isInstanceOf(CodingApiException.class)
				.hasMessageContaining("please retry");

		verify(studentRepository).findByIdWithLock(1L);
		verify(teacherRepository).findByIdWithLock(1L);

		verify(lessonRepository).save(lessonCaptor.capture());
		Lesson saved = lessonCaptor.getValue();
		assertThat(saved).isNotNull();
		assertThat(saved.getId()).isEqualTo(lessonId);
		assertThat(saved.getDate()).isEqualTo(newDate);
	}
}

class LessonMapperTest {

	private static final LocalDateTime NOW = LocalDateTime.of(2025, 6, 1, 10, 0);

	@Test
	void toDto_shouldMapAllFieldsCorrectly() {

		Teacher teacher = createTeacher(Set.of(PL, EN));
		Student student = createStudent(teacher);

		Lesson lesson = new Lesson();
		lesson.setId(100L);
		lesson.setTeacher(teacher);
		lesson.setStudent(student);
		lesson.setDate(NOW);

		LessonDTO dto = LessonMapper.toDto(lesson);

		assertThat(dto).isNotNull().hasFieldOrPropertyWithValue("id", 100L).hasFieldOrPropertyWithValue("date", NOW);

		assertThat(dto.student()).hasFieldOrPropertyWithValue("id", 10L)
				.hasFieldOrPropertyWithValue("firstName", "Jan")
				.hasFieldOrPropertyWithValue("lastName", "Nowak")
				.hasFieldOrPropertyWithValue("language", PL)
				.hasFieldOrPropertyWithValue("teacherId", 1L);

		assertThat(dto.teacher()).hasFieldOrPropertyWithValue("id", 1L)
				.hasFieldOrPropertyWithValue("firstName", "Anna")
				.hasFieldOrPropertyWithValue("lastName", "Kowalska")
				.hasFieldOrPropertyWithValue("languages", Set.of(PL, EN));
	}

}