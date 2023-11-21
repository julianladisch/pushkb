package org.dcb.test;

import java.util.List;

import org.dcb.test.InstanceIndexer.Author;
import org.dcb.test.InstanceIndexer.InstanceDocument;

public class InstanceExamples {
	public static InstanceDocument brainOfTheFirm() {
		return InstanceDocument.builder()
			.title("Brain of the Firm")
			.authors(List.of(namedAuthor("Stafford Beer")))
			.subjects(List.of("Cybernetics", "Industrial management"))
			.publicationDate("1972")
			.publicationYear(1972)
			.build();
	}

	public static InstanceDocument surfaceDetail() {
		return InstanceDocument.builder()
			.title("Surface detail")
			.isbns(List.of("9780316123402"))
			.publicationYear(2010)
			.publicationDate("2010")
			.authors(List.of(namedAuthor("Banks, Iain")))
			.subjects(List.of("Fiction", "Science fiction"))
			.build();
	}

	public static InstanceDocument science() {
		return InstanceDocument.builder()
			.title("Science")
			.issns(List.of("0036-8075"))
			.build();
	}

	public static InstanceDocument nature() {
		return InstanceDocument.builder()
			.title("Nature")
			.issns(List.of("0028-0836", "1476-4687"))
			.build();
	}

	public static InstanceDocument trainingDay() {
		return InstanceDocument.builder()
			.title("Training Day")
			.format("DVD")
			.build();
	}

	public static InstanceDocument prisonersOfGeography() {
		return InstanceDocument.builder()
			.title("Prisoners of geography")
			.isbns(List.of("9781783962433"))
			.publisher(elliottAndThompsonLimited())
			.publicationDate("2019")
			.publicationYear(2019)
			.subjects(List.of("Geopolitics", "Politics and Government", "Geography"))
			.authors(List.of(
				namedAuthor("Tim Marshall"),
				namedAuthor("John Scarlett")))
			.format(book())
			.physicalDescriptions(List.of("303 pages"))
			.build();
	}

	public static InstanceDocument practicalGeography() {
		return InstanceDocument.builder()
			.title("Practical geography")
			.isbns(List.of("9789386317865"))
			.publisher("EBH Publishers")
			.publicationDate("2018")
			.publicationYear(2018)
			.subjects(List.of("Geography"))
			.authors(List.of(namedAuthor("Saikia, R.")))
			.format("eBook")
			.build();
	}

	public static InstanceDocument scientificAmerican() {
		return InstanceDocument.builder()
			.title("Scientific American")
			.issns(List.of("0036-8733"))
			.format("Journal")
			.build();
	}

	public static InstanceDocument guardsGuards() {
		return InstanceDocument.builder()
			.title("Guards! Guards!")
			.isbns(List.of("9780061020643", "0061020648"))
			.authors(List.of(namedAuthor("Pratchett, Terry")))
			.publisher("Harper, New York")
			.publicationDate("2008, ©1989")
			.publicationYear(2008)
			.subjects(List.of("Fiction"))
			.format(book())
			.physicalDescriptions(List.of("355 pages"))
			.build();
	}

	public static InstanceDocument beneathTheSurface() {
		return InstanceDocument.builder()
			.title("Beneath the Surface")
			.isbns(List.of("9781534199781"))
			.authors(List.of(
				namedAuthor("Jason M. Burns"),
				namedAuthor("Dustin Evans")))
			.build();
	}

	public static InstanceDocument wholeBrainChild() {
		return InstanceDocument.builder()
			.title("The Whole-Brain Child")
			.isbns(List.of("9781780338378"))
			.authors(List.of(
				namedAuthor("Daniel J. Siegel"),
				namedAuthor("Tina Payne Bryson")))
			.build();
	}

	public static InstanceDocument motherTongue() {
		return InstanceDocument.builder()
			.title("Mother tongue")
			.isbns(List.of("9780380715435"))
			.authors(List.of(namedAuthor("Bryson, Bill")))
			.build();
	}

	public static InstanceDocument treasureEverywhere() {
		return InstanceDocument.builder()
			.title("There's treasure everywhere")
			.isbns(List.of("9780836213126"))
			.authors(List.of(namedAuthor("Watterson, Bill")))
			.build();
	}

	public static InstanceDocument educationOfAnIdealist() {
		return InstanceDocument.builder()
			.title("Education of an idealist")
			.authors(List.of(namedAuthor("Samantha Power")))
			.isbns(List.of("9780008274900"))
			.subjects(List.of("Politics and Government", "Autobiographies"))
			.build();
	}

	public static InstanceDocument darwinsArmada() {
		return InstanceDocument.builder()
			.title("Darwin's armada")
			.authors(List.of(namedAuthor("Iain McCalman")))
			.isbns(List.of("9781847372666"))
			.subjects(List.of("Biographies", "Evolution (Biology)"))
			.build();
	}

	public static InstanceDocument lairOfTheLion() {
		return InstanceDocument.builder()
			.title("Lair of the lion")
			.authors(List.of(namedAuthor("Christine Feehan")))
			.isbns(List.of("9780749958459"))
			.publisher("Piatkus, London")
			.publicationDate("2012")
			.publicationYear(2012)
			.subjects(List.of("Fiction", "Historical fiction", "Romance fiction", "Prisoners"))
			.format(book())
			.build();
	}

	public static InstanceDocument cityOfMirrors() {
		return InstanceDocument.builder()
			.title("City of Mirrors")
			.authors(List.of(namedAuthor("Melodie Johnson Howe")))
			.isbns(List.of("9781909653948"))
			.publisher(elliottAndThompsonLimited())
			.subjects(List.of("Fiction", "California Los Angeles"))
			.format(book())
			.build();
	}

	public static InstanceDocument autumn() {
		return InstanceDocument.builder()
			.title("Autumn")
			.authors(List.of(namedAuthor("Melissa Harrison")))
			.isbns(List.of("9781783962488"))
			.publisher(elliottAndThompsonLimited())
			.subjects(List.of("Autumn", "Natural History"))
			.format(book())
			.build();
	}

	public static InstanceDocument hamnet() {
		return InstanceDocument.builder()
			.title("Hamnet")
			.authors(List.of(namedAuthor("Maggie O'Farrell")))
			.isbns(List.of("9781472223821"))
			.publisher("Headline Publishing Group")
			.subjects(List.of("Fiction", "Historical Fiction"))
			.format(book())
			.build();
	}

	public static InstanceDocument blankDocument() {
		return InstanceDocument.builder()
			.title("")
			.authors(List.of(Author.builder()
				.name("")
				.build()))
			.build();
	}

	public static InstanceDocument emptyDocument() {
		return InstanceDocument.builder()
			.build();
	}

	private static String book() {
		return "Book";
	}

	private static String elliottAndThompsonLimited() {
		return "Elliott and Thompson Limited";
	}

	private static Author namedAuthor(String name) {
		return Author.builder().name(name).build();
	}
}
