import clsx from 'clsx';
import Heading from '@theme/Heading';
import styles from './styles.module.css';

type FeatureItem = {
  title: string;
  Svg: React.ComponentType<React.ComponentProps<'svg'>>;
  description: JSX.Element;
};

const FeatureList: FeatureItem[] = [
  {
    title: 'Easy Multi-Source Config Management',
    Svg: require('@site/static/img/undraw_docusaurus_mountain.svg').default,
    description: (
      <>
        Gestalt enables easy merging of configurations from multiple sources (files, environment variables, maps) into a unified structure, simplifying how you manage application settings.
      </>
    ),
  },
  {
    title: 'Type-Safe Automatic Decoding',
    Svg: require('@site/static/img/undraw_docusaurus_tree.svg').default,
    description: (
      <>
        It automatically decodes configurations into Java objects, including beans, records, and lists, supporting Java and Kotlin, while providing error feedback for missing or invalid configs.
      </>
    ),
  },
  {
    title: 'Modular and Lightweight',
    Svg: require('@site/static/img/undraw_docusaurus_react.svg').default,
    description: (
      <>
        With zero core dependencies, Gestalt allows you to include only the necessary features, keeping your application lightweight and configurable for various environments or profiles.
      </>
    ),
  },
];

function Feature({title, Svg, description}: FeatureItem) {
  return (
    <div className={clsx('col col--4')}>
      <div className="text--center">
        <Svg className={styles.featureSvg} role="img" />
      </div>
      <div className="text--center padding-horiz--md">
        <Heading as="h3">{title}</Heading>
        <p>{description}</p>
      </div>
    </div>
  );
}

export default function HomepageFeatures(): JSX.Element {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className="row">
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
        <div className="features">
        <p></p>
        <Heading as="h3" className="text--center padding-horiz--md">Features</Heading>
        <ul>
          <li><b>Automatic decoding based on type</b>: Supports decoding into bean classes, lists, sets, or primitive types. This simplifies configuration retrieval.</li>
          <li><b>Java Records</b>: Full support for Java Records, constructing records from configuration using the Records Canonical Constructor.</li>
          <li><b>Supports Multiple Formats</b>: Load configurations from various sources, including Environment Variables, Property files, an in-memory map, and more.</li>
          <li><b>Read Sub-sections of Your Config</b>: Easily navigate to specific sub-sections within configurations using dot notation.</li>
          <li><b>Kotlin interface</b>: Full support for Kotlin with an easy-to-use Kotlin-esque interface, ideal for Kotlin projects.</li>
          <li><b>Merge Multiple Sources</b>: Seamlessly merge configurations from different sources to create comprehensive settings.</li>
          <li><b>String Substitution</b>: Build a config value by injecting Environment Variables, System Properties or other nodes into your strings.</li>
          <li><b>node Substitution</b>: Include whole config nodes loaded from files or other places in the config tree anywhere in your config tree.</li>
          <li><b>A/B Testing</b>: Segregate results based on groups or random results. See A/B Testing in the use cases section.</li>
          <li><b>Flexible and Configurable</b>: The library offers well-defined interfaces, allowing customization and extension.</li>
          <li><b>Easy-to-Use Builder</b>: Get started quickly with a user-friendly builder, or customize specific aspects of the library.</li>
          <li><b>Receive All Errors Up Front</b>: In case of configuration errors, receive multiple errors in a user-friendly log for efficient debugging.</li>
          <li><b>Modular Support for Features</b>: Include only the required features and dependencies in your build, keeping your application lightweight.</li>
          <li><b>Zero Dependencies</b>: The core library has zero external dependencies; add features and dependencies as needed.</li>
          <li><b>Java 11 Minimum</b>: Requires a minimum of Java 11 for compatibility with modern Java versions.</li>
          <li><b>Java Modules</b>: Supports Java 9 modules with proper exports.</li>
          <li><b>Well Tested</b>: Our codebase boasts an impressive > 91% code coverage, validated by over 1850 meaningful tests.</li>
        </ul>
        </div>
      </div>
    </section>
  );
}
